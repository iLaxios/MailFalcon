package com.laxios.MailFalcon.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;
import com.laxios.MailFalcon.model.EmailStatus;
import com.laxios.MailFalcon.repository.EmailRepository;
import com.laxios.MailFalcon.service.EmailService;
import com.laxios.MailFalcon.service.RedisQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository repository;
    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;

    @Override
    public void queueMail(EmailRequest emailRequest) {
        String id = UUID.randomUUID().toString();

        EmailRecord emailRecord = new EmailRecord();
        emailRecord.setId(id);
        emailRecord.setTo(emailRequest.getTo());
        emailRecord.setSubject(emailRequest.getSubject());
        emailRecord.setBody(emailRequest.getBody());
        emailRecord.setStatus(EmailStatus.QUEUED);
        emailRecord.setRetryCount(0);
        emailRecord.setCreatedAt(LocalDateTime.now());

        repository.save(emailRecord);
        emailRequest.setRecordId(id);

        redisQueueService.enqueueMail(emailRecord);
        log.info("Email to {} queued successfully", emailRecord.getTo());
    }

    @Override
    @Async("mailExecutor")
    public void sendMail(EmailRecord emailRecord) {

        emailRecord.setStatus(EmailStatus.SENDING);
        String id = emailRecord.getId();
        try {
            repository.save(emailRecord);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRecord.getTo());
            message.setSubject(emailRecord.getSubject());
            message.setText(emailRecord.getBody());
            message.setFrom("test@email.com");
            mailSender.send(message);

            emailRecord.setStatus(EmailStatus.SENT);
            emailRecord.setSentAt(LocalDateTime.now());
            log.info("Email [{}] sent successfully", id);

        } catch (Exception e) {

            emailRecord.setErrorMessage(e.getMessage());
            int MAX_RETRIES = 3;
            if(emailRecord.getRetryCount() >= MAX_RETRIES) {
                emailRecord.setStatus(EmailStatus.FAILED_PERMANANTLY);
                log.error("Email [{}] permanantly failed to be sent: {}", id, e.getMessage());
            }

            else {

                emailRecord.setStatus(EmailStatus.FAILED);
                emailRecord.setRetryCount(emailRecord.getRetryCount() + 1);
                log.error("Email [{}] failed to send: {}", id, e.getMessage());

                // Enqueue to retry queue
                redisQueueService.enqueueRetry(emailRecord);
                log.warn("Email [{}] moved to retry queue", id);

            }
        }

        repository.save(emailRecord);
    }

    public List<EmailRecord> getAllEmails() {
        return new ArrayList<>(repository.findAll().values());
    }
}

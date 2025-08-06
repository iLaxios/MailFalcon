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

        EmailRecord record = new EmailRecord();
        record.setId(id);
        record.setTo(emailRequest.getTo());
        record.setSubject(emailRequest.getSubject());
        record.setBody(emailRequest.getBody());
        record.setStatus(EmailStatus.QUEUED);
        record.setRetryCount(0);
        record.setCreatedAt(LocalDateTime.now());

        repository.save(record);
        emailRequest.setRecordId(id);

        redisQueueService.enqueueMail(emailRequest);
        log.info("Email to {} queued successfully", emailRequest.getTo());
    }

    @Override
    @Async("mailExecutor")
    public void sendMail(EmailRequest emailRequest) {

        String id = emailRequest.getRecordId();
        EmailRecord record = repository.findById(id);
        record.setStatus(EmailStatus.SENDING);
        try {
            repository.save(record);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRequest.getTo());
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());
            message.setFrom("test@email.com");
            mailSender.send(message);

            record.setStatus(EmailStatus.SENT);
            record.setSentAt(LocalDateTime.now());
            log.info("Email [{}] sent successfully", id);

        } catch (Exception e) {

            record.setErrorMessage(e.getMessage());
            int MAX_RETRIES = 3;
            if(record.getRetryCount() >= MAX_RETRIES) {
                record.setStatus(EmailStatus.FAILED_PERMANANTLY);
                log.error("Email [{}] permanantly failed to be sent: {}", id, e.getMessage());
            }

            else {

                record.setStatus(EmailStatus.FAILED);
                record.setRetryCount(record.getRetryCount() + 1);
                log.error("Email [{}] failed to send: {}", id, e.getMessage());

                // Enqueue to retry queue
                redisQueueService.enqueueRetry(emailRequest);
                log.warn("Email [{}] moved to retry queue", id);

            }
        }

        repository.save(record);
    }

    public List<EmailRecord> getAllEmails() {
        return new ArrayList<>(repository.findAll().values());
    }
}

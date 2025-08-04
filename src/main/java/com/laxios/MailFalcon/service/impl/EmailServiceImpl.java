package com.laxios.MailFalcon.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        try {
            String payload = objectMapper.writeValueAsString(emailRequest);
            redisQueueService.enqueueMail(payload);
            log.info("Email to {} queued successfully", emailRequest.getTo());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize email request", e);
        }
    }

    @Override
    @Async("mailExecutor")
    public void sendMail(EmailRequest emailRequest) {
        String id = UUID.randomUUID().toString();

        EmailRecord record = new EmailRecord();
        record.setId(id);
        record.setTo(emailRequest.getTo());
        record.setSubject(emailRequest.getSubject());
        record.setBody(emailRequest.getBody());
        record.setStatus(EmailStatus.SENDING);
        record.setRetryCount(0);
        record.setCreatedAt(LocalDateTime.now());

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
            record.setStatus(EmailStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            log.error("Email [{}] failed to send: {}", id, e.getMessage());

            // Enqueue to retry queue
            try {
                String payload = objectMapper.writeValueAsString(emailRequest);
                redisQueueService.enqueueRetry(payload);
                log.warn("Email [{}] moved to retry queue", id);
            } catch (JsonProcessingException jsonEx) {
                log.error("Retry payload serialization failed", jsonEx);
            }
        }

        repository.save(record);
    }

    public List<EmailRecord> getAllEmails() {
        return new ArrayList<>(repository.findAll().values());
    }
}

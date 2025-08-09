package com.laxios.MailFalcon.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;
import com.laxios.MailFalcon.model.EmailStatus;
import com.laxios.MailFalcon.repository.EmailJpaRepository;
import com.laxios.MailFalcon.repository.EmailRedisRepository;
import com.laxios.MailFalcon.service.EmailService;
import com.laxios.MailFalcon.service.RedisQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailRedisRepository emailRedisRepository;
    private final EmailJpaRepository emailJpaRepository;
    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;

    @Override
    public void queueMail(EmailRequest emailRequest) {
        String id = UUID.randomUUID().toString();

        EmailRecord emailRecord = new EmailRecord();
        emailRecord.setId(id);
        emailRecord.setRecipient(emailRequest.getTo());
        emailRecord.setSubject(emailRequest.getSubject());
        emailRecord.setBody(emailRequest.getBody());
        emailRecord.setStatus(EmailStatus.QUEUED);
        emailRecord.setRetryCount(0);
        emailRecord.setCreatedAt(LocalDateTime.now());

        emailRedisRepository.save(emailRecord);
        emailRequest.setRecordId(id);

        redisQueueService.enqueueMail(emailRecord);
        log.info("Email to {} queued successfully", emailRecord.getRecipient());
    }

    @Override
    @Async("mailExecutor")
    public void sendMail(EmailRecord emailRecord) {

        emailRecord.setStatus(EmailStatus.SENDING);
        String id = emailRecord.getId();
        try {
            emailRedisRepository.save(emailRecord);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRecord.getRecipient());
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

                final int BASE_DELAY_SECONDS = 60;
                emailRecord.setStatus(EmailStatus.FAILED);
                emailRecord.setRetryCount(emailRecord.getRetryCount() + 1);
                log.error("Email [{}] failed to send: {}", id, e.getMessage());

                // Enqueue to retry queue
                long delay = (long) Math.pow(2, emailRecord.getRetryCount()) * BASE_DELAY_SECONDS;
                redisQueueService.enqueueRetryWithDelay(emailRecord, delay);
                log.warn("Email [{}] moved to retry queue", id);

            }
        }

        emailJpaRepository.save(emailRecord);
        emailRedisRepository.delete(id);
    }

    public EmailRecord getEmailById(String id) {
        // First check Redis for transient states
        EmailRecord email = emailRedisRepository.findById(id);
        if(email == null) {
            // try to fetch from DB
            email = emailJpaRepository.findById(id).orElse(null);
            if(email == null) {
                log.error("{}:Email could not be found in DB", id);
            }
            else {
                log.info("{}:Found in DB", id);
            }
        }
        else {
            log.info("{}:Found in redis cache", id);
        }
        return email;
    }
}

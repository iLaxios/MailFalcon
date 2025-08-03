package com.laxios.MailFalcon.service.impl;

import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;
import com.laxios.MailFalcon.model.EmailStatus;
import com.laxios.MailFalcon.service.EmailService;
import com.laxios.MailFalcon.repository.EmailRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final EmailRepository emailRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    @Async("mailExecutor")
    public void sendSimpleMessage(EmailRequest emailRequest) {
        System.out.println("Sending email to: " + emailRequest.getTo() + " on thread: " + Thread.currentThread().getName());

        String id = UUID.randomUUID().toString();
        EmailRecord record = new EmailRecord();
        record.setId(id);
        record.setTo(emailRequest.getTo());
        record.setSubject(emailRequest.getSubject());
        record.setBody(emailRequest.getBody());
        record.setStatus(EmailStatus.QUEUED);
        record.setRetryCount(0);
        record.setCreatedAt(LocalDateTime.now());

        emailRepository.save(record);
        logger.info("Email [{}] queued", id);

        try {
            record.setStatus(EmailStatus.SENDING);
            emailRepository.save(record);
            logger.info("Email [{}] sending...", id);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRequest.getTo());
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());
            message.setFrom("test@email.com");
            emailSender.send(message);

            record.setStatus(EmailStatus.SENT);
            record.setSentAt(LocalDateTime.now());
            logger.info("Email [{}] sent successfully", id);
        } catch (Exception e) {
            record.setStatus(EmailStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            logger.error("Email [{}] failed: {}", id, e.getMessage());
        }

        emailRepository.save(record);
    }

    @Override
    public List<EmailRecord> getAllEmails() {
        return new ArrayList<>(emailRepository.findAll().values());
    }
}

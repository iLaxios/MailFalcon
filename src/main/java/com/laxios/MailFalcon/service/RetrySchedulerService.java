package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.model.EmailRecord;
import com.laxios.MailFalcon.model.EmailStatus;
import com.laxios.MailFalcon.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetrySchedulerService {

    private final EmailRepository repository;
    private final JavaMailSender mailSender;

    private static final int MAX_RETRIES = 3;

    @Scheduled(fixedDelay = 10000) // every 10 seconds
    public void retryFailedEmails() {
        for (Map.Entry<String, EmailRecord> entry : repository.findAll().entrySet()) {
            EmailRecord record = entry.getValue();

            if (record.getStatus() == EmailStatus.FAILED && record.getRetryCount() < MAX_RETRIES) {
                try {
                    log.info("Retrying email to {}", record.getTo());

                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(record.getTo());
                    message.setSubject(record.getSubject());
                    message.setText(record.getBody());
                    mailSender.send(message);

                    record.setStatus(EmailStatus.SENT);
                    repository.save(record);
                    log.info("Retried and sent: {}", record.getId());

                } catch (Exception e) {
                    record.setRetryCount(record.getRetryCount() + 1);
                    log.warn("Retry failed for {} (attempt {}): {}", record.getTo(), record.getRetryCount(), e.getMessage());

                    if (record.getRetryCount() >= MAX_RETRIES) {
                        record.setStatus(EmailStatus.FAILED);
                        log.error("Max retries exceeded for {}", record.getId());
                    }
                    repository.save(record);
                }
            }
        }
    }
}

package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryMailQueueProcessor {

    private final RedisQueueService RedisQueueService;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 10000)
    public void processRetryQueue() {
        EmailRecord emailRecord;
        while ((emailRecord = RedisQueueService.dequeueRetry()) != null) {
            try {
                emailService.sendMail(emailRecord); // Async method
            } catch (Exception e) {
                log.error("Retry processing failed, re-queuing", e);
                RedisQueueService.enqueueRetry(emailRecord);
            }
        }
    }
}

package com.laxios.MailFalcon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.service.EmailService;
import com.laxios.MailFalcon.service.RedisQueueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailQueueProcessor {

    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void startProcessing() {
        executor.submit(() -> {
            while (true) {
                try {
                    String payload = redisQueueService.dequeueMail();
                    if (payload != null) {
                        EmailRequest request = objectMapper.readValue(payload, EmailRequest.class);
                        emailService.sendMail(request);
                    } else {
                        Thread.sleep(100); // brief pause to avoid busy waiting
                    }
                } catch (Exception e) {
                    log.error("Error processing mail queue", e);
                    Thread.sleep(100); // avoid spinning on exceptions
                }
            }
        });
    }
}

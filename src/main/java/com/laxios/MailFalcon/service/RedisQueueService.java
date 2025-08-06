package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, EmailRequest> emailRequestRedisTemplate;

    private static final String MAIN_QUEUE = "mailQueue";
    private static final String RETRY_QUEUE = "mailRetryQueue";

    public void enqueueMail(EmailRequest request) {
        emailRequestRedisTemplate.opsForList().rightPush(MAIN_QUEUE, request);
    }

    public EmailRequest dequeueMail() {
        return emailRequestRedisTemplate.opsForList().leftPop(MAIN_QUEUE);
    }

    public void enqueueRetry(EmailRequest request) {
        emailRequestRedisTemplate.opsForList().rightPush(RETRY_QUEUE, request);
    }

    public EmailRequest dequeueRetry() {
        return emailRequestRedisTemplate.opsForList().leftPop(RETRY_QUEUE);
    }
}


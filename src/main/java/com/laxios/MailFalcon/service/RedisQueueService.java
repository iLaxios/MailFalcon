package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.model.EmailRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, EmailRecord> emailRequestRedisTemplate;

    private static final String MAIN_QUEUE = "mailQueue";
    private static final String RETRY_QUEUE = "mailRetryQueue";

    public void enqueueMail(EmailRecord request) {
        emailRequestRedisTemplate.opsForList().rightPush(MAIN_QUEUE, request);
    }

    public EmailRecord dequeueMail() {
        return emailRequestRedisTemplate.opsForList().leftPop(MAIN_QUEUE);
    }

    public void enqueueRetry(EmailRecord request) {
        emailRequestRedisTemplate.opsForList().rightPush(RETRY_QUEUE, request);
    }

    public EmailRecord dequeueRetry() {
        return emailRequestRedisTemplate.opsForList().leftPop(RETRY_QUEUE);
    }
}


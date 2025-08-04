package com.laxios.MailFalcon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final StringRedisTemplate redisTemplate;

    private final String MAIN_QUEUE = "mailQueue";
    private final String RETRY_QUEUE = "mailRetryQueue";

    public void enqueueMail(String payload) {
        redisTemplate.opsForList().rightPush(MAIN_QUEUE, payload);
    }

    public String dequeueMail() {
        return redisTemplate.opsForList().leftPop(MAIN_QUEUE);
    }

    public void enqueueRetry(String payload) {
        redisTemplate.opsForList().rightPush(RETRY_QUEUE, payload);
    }

    public String dequeueRetry() {
        return redisTemplate.opsForList().leftPop(RETRY_QUEUE);
    }
}

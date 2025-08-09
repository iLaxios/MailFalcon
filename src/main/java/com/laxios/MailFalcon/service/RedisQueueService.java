package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.model.EmailRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, EmailRecord> emailRedisTemplate;

    private static final String MAIN_QUEUE = "mailQueue";
    private static final String RETRY_QUEUE = "mailRetryQueue";

    public void enqueueMail(EmailRecord request) {
        emailRedisTemplate.opsForList().rightPush(MAIN_QUEUE, request);
    }

    public EmailRecord dequeueMail() {
        return emailRedisTemplate.opsForList().leftPop(MAIN_QUEUE);
    }

    public void enqueueRetryWithDelay(EmailRecord record, long delaySeconds) {
        long score = System.currentTimeMillis() / 1000 + delaySeconds;
        emailRedisTemplate.opsForZSet().add(RETRY_QUEUE, record, score);
    }

    public EmailRecord dequeueRetryReady() {
        long now = System.currentTimeMillis() / 1000;
        Set<EmailRecord> readyItems = emailRedisTemplate.opsForZSet().rangeByScore(RETRY_QUEUE, 0, now, 0, 1);
        if (readyItems == null || readyItems.isEmpty()) return null;

        EmailRecord item = readyItems.iterator().next();
        // Remove it so it’s not processed again
        emailRedisTemplate.opsForZSet().remove(RETRY_QUEUE, item);
        return item;
    }
}


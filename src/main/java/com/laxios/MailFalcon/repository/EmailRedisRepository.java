package com.laxios.MailFalcon.repository;

import com.laxios.MailFalcon.model.EmailRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmailRedisRepository {

    private static final String PREFIX = "email:"; // Key prefix to avoid collisions
    private final RedisTemplate<String, EmailRecord> redisTemplate;

    public EmailRedisRepository(RedisTemplate<String, EmailRecord> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(EmailRecord record) {
        redisTemplate.opsForValue().set(PREFIX + record.getId(), record);
    }

    public EmailRecord findById(String id) {
        return redisTemplate.opsForValue().get(PREFIX + id);
    }

    public boolean exists(String id) {
        return redisTemplate.hasKey(PREFIX + id);
    }

    public void delete(String id) {
        redisTemplate.delete(PREFIX + id);
    }
}

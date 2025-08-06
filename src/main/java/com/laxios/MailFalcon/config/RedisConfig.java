package com.laxios.MailFalcon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laxios.MailFalcon.dto.EmailRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // Defaults to localhost:6379
    }

    @Bean
    public RedisTemplate<String, EmailRequest> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, EmailRequest> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<EmailRequest> serializer =
                new Jackson2JsonRedisSerializer<>(EmailRequest.class);

        ObjectMapper mapper = new ObjectMapper();
        serializer.setObjectMapper(mapper);

        template.setDefaultSerializer(serializer);
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

}

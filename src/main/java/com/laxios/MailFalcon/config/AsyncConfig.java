package com.laxios.MailFalcon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // number of core threads
        executor.setMaxPoolSize(10);      // max threads
        executor.setQueueCapacity(100);   // waiting queue size
        executor.setThreadNamePrefix("MailSender-");
        executor.initialize();
        return executor;
    }
}

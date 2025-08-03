package com.laxios.MailFalcon.service.impl;

import com.laxios.MailFalcon.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    @Async("mailExecutor")
    public void sendSimpleMessage(String to, String subject, String text) {
        System.out.println("Sending email to: " + to + " on thread: " + Thread.currentThread().getName());
        logger.info("Preparing to send email to {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("test@email.com");
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
        logger.info("Email sent to {}", to);
    }
}

package com.laxios.MailFalcon.controller;

import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendMail(@RequestBody EmailRequest emailRequest) {

        emailService.sendSimpleMessage(emailRequest);
        return "Email sent to " + emailRequest.getTo();
    }
}

package com.laxios.MailFalcon.controller;

import com.laxios.MailFalcon.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendMail(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body) {
        emailService.sendSimpleMessage(to, subject, body);
        return "Email sent to " + to;
    }
}

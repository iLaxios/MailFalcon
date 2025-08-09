package com.laxios.MailFalcon.controller;

import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;
import com.laxios.MailFalcon.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendMail(@RequestBody EmailRequest emailRequest) {

        emailService.queueMail(emailRequest);
        return "Email sent to " + emailRequest.getTo();
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<EmailRecord> getEmailById(@PathVariable("id") String id) {
        EmailRecord email = emailService.getEmailById(id);
        return email != null ? ResponseEntity.ok(email) : ResponseEntity.notFound().build();
    }
}

package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.dto.EmailRequest;
import com.laxios.MailFalcon.model.EmailRecord;

import java.util.List;

public interface EmailService {
    void queueMail(EmailRequest emailRequest);
    void sendMail(EmailRequest emailRequest);
    List<EmailRecord> getAllEmails();
}

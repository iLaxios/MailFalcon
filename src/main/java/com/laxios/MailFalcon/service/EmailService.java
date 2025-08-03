package com.laxios.MailFalcon.service;

import com.laxios.MailFalcon.dto.EmailRequest;

public interface EmailService {
    void sendSimpleMessage(EmailRequest emailRequest);
}

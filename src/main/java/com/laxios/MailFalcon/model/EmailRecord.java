package com.laxios.MailFalcon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRecord {
    private String id; // UUID
    private String to;
    private String subject;
    private String body;
    private EmailStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
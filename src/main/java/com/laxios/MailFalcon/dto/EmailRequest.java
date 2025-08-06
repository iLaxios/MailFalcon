package com.laxios.MailFalcon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest implements Serializable {
    private String to;
    private String subject;
    private String body;
    private String recordId;
}
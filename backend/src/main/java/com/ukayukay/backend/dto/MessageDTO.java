package com.ukayukay.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDTO {
    private Integer messageId;
    private Integer senderId;
    private Integer recipientId;
    private String senderName;
    private String recipientName;
    private String subject;
    private String messageContent;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

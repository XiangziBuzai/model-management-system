package com.model.management.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatMessageVO {
    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}

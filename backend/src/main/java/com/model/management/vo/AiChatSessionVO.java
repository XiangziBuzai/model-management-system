package com.model.management.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatSessionVO {
    private Long id;
    private String title;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

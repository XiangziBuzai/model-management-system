package com.model.management.dto;

import lombok.Data;

/**
 * 发送消息请求DTO
 */
@Data
public class MessageSendDTO {
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
}

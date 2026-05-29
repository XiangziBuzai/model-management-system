package com.model.management.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息VO（包含用户信息）
 */
@Data
public class MessageVO {
    
    private Long id;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String content;
    
    private Integer isRead;
    
    /**
     * 发送者昵称
     */
    private String senderNickname;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者昵称
     */
    private String receiverNickname;
    
    /**
     * 接收者头像
     */
    private String receiverAvatar;
    
    /**
     * 发送时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否已撤回: 0-未撤回, 1-已撤回
     */
    private Integer isRecalled;

    /**
     * 原始内容（用于撤回后编辑重发）
     */
    private String originalContent;

    /**
     * 该对话中当前用户的未读消息数
     */
    private Integer unreadCount;
}

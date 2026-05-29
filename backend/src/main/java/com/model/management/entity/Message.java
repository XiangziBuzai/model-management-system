package com.model.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息实体类
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 是否已撤回: 0-未撤回, 1-已撤回
     */
    private Integer isRecalled;

    /**
     * 原始内容（用于撤回后编辑重发）
     */
    private String originalContent;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

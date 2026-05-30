-- AI 助手功能数据库迁移脚本
-- 创建时间: 2026-05-30

-- 1. 创建 AI 对话会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) COMMENT '会话标题',
    `last_message` TEXT COMMENT '最后一条消息摘要',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话会话表';

-- 2. 创建 AI 对话消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user-用户, assistant-AI助手',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话消息表';

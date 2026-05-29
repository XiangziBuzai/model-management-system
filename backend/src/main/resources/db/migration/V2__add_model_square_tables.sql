-- 模型广场功能数据库迁移脚本
-- 创建时间: 2026-05-28

-- 1. 为model和tool表添加公开状态字段
ALTER TABLE `model` ADD COLUMN `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公开: 0-私有, 1-公开' AFTER `userId`;
ALTER TABLE `tool` ADD COLUMN `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公开: 0-私有, 1-公开' AFTER `userId`;

-- 2. 创建收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_type` VARCHAR(20) NOT NULL COMMENT '物品类型: MODEL-模型, TOOL-工具',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_type`, `item_id`) COMMENT '用户不能重复收藏同一物品',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item` (`item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 3. 创建私信消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_unread` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 4. 创建交易订单表
CREATE TABLE IF NOT EXISTS `transaction_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `item_type` VARCHAR(20) NOT NULL COMMENT '物品类型: MODEL-模型, TOOL-工具',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `item_name` VARCHAR(200) NOT NULL COMMENT '物品名称（快照）',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态: PENDING-待处理, COMPLETED-已完成, CANCELLED-已取消',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 5. 创建价格变动通知表
CREATE TABLE IF NOT EXISTS `price_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_type` VARCHAR(20) NOT NULL COMMENT '物品类型: MODEL-模型, TOOL-工具',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `last_price` DECIMAL(10, 2) NOT NULL COMMENT '上次记录的价格',
    `current_price` DECIMAL(10, 2) NOT NULL COMMENT '当前价格',
    `is_notified` TINYINT(1) DEFAULT 0 COMMENT '是否已通知: 0-未通知, 1-已通知',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_type`, `item_id`),
    KEY `idx_unnotified` (`is_notified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格变动通知表';

-- 6. 为model和tool表添加索引以优化公开查询性能
CREATE INDEX idx_model_public ON `model` (`is_public`, `deleted`);
CREATE INDEX idx_tool_public ON `tool` (`is_public`, `deleted`);

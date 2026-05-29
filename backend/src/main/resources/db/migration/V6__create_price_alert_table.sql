-- 创建价格提醒表
CREATE TABLE IF NOT EXISTS price_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_type VARCHAR(20) NOT NULL COMMENT '物品类型: MODEL-模型, TOOL-工具',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    last_price DECIMAL(10,2) COMMENT '上次记录的价格',
    current_price DECIMAL(10,2) COMMENT '当前价格',
    is_notified TINYINT DEFAULT 0 COMMENT '是否已通知: 0-未通知, 1-已通知',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_item_type_id (item_type, item_id),
    INDEX idx_is_notified (is_notified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格变动提醒表';

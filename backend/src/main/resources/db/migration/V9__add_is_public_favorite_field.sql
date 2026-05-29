-- 添加收藏列表公开设置字段
ALTER TABLE user ADD COLUMN is_public_favorite INT DEFAULT 0 COMMENT '是否公开收藏列表: 0-私密, 1-公开';

-- 为模型表添加浏览量和收藏数字段
ALTER TABLE model 
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量',
ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数';

-- 为工具表添加浏览量和收藏数字段
ALTER TABLE tool 
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量',
ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数';

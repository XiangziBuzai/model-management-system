-- 添加消息撤回相关字段
ALTER TABLE message 
ADD COLUMN is_recalled INT DEFAULT 0 COMMENT '是否已撤回: 0-未撤回, 1-已撤回',
ADD COLUMN original_content TEXT COMMENT '原始内容（用于撤回后编辑重发）';

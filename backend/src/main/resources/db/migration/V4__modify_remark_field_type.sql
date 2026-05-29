-- 修改 remark 字段类型以支持富文本和图片
-- 创建时间: 2026-05-28

-- 修改 model 表的 remark 字段为 LONGTEXT 类型
ALTER TABLE `model` MODIFY COLUMN `remark` LONGTEXT COMMENT '备注';

-- 修改 tool 表的 remark 字段为 LONGTEXT 类型
ALTER TABLE `tool` MODIFY COLUMN `remark` LONGTEXT COMMENT '备注';

-- 修改 transaction_order 表的 remark 字段为 LONGTEXT 类型
ALTER TABLE `transaction_order` MODIFY COLUMN `remark` LONGTEXT COMMENT '备注';

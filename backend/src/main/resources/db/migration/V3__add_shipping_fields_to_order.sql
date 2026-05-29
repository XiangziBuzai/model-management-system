-- 为交易订单表添加物流相关字段
ALTER TABLE `transaction_order` 
ADD COLUMN `tracking_number` VARCHAR(50) COMMENT '快递单号' AFTER `remark`,
ADD COLUMN `express_company` VARCHAR(50) COMMENT '快递公司' AFTER `tracking_number`;

-- 更新订单状态注释，添加新的状态值
ALTER TABLE `transaction_order` 
MODIFY COLUMN `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态: PENDING-待发货, SHIPPED-已发货(待收货), COMPLETED-已完成, CANCELLED-已取消';

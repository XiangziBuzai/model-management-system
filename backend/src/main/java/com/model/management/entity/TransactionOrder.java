package com.model.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单实体类
 */
@Data
@TableName("transaction_order")
public class TransactionOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 物品类型: MODEL-模型, TOOL-工具
     */
    private String itemType;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品名称（快照）
     */
    private String itemName;

    /**
     * 成交价格
     */
    private BigDecimal price;

    /**
     * 订单状态: PENDING-待发货, SHIPPED-已发货(待收货), COMPLETED-已完成, CANCELLED-已取消
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

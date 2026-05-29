package com.model.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格变动通知实体类
 */
@Data
@TableName("price_alert")
public class PriceAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品类型: MODEL-模型, TOOL-工具
     */
    private String itemType;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 上次记录的价格
     */
    private BigDecimal lastPrice;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 是否已通知: 0-未通知, 1-已通知
     */
    private Integer isNotified;

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

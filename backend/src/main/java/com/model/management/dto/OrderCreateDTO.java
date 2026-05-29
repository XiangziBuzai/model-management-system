package com.model.management.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求DTO
 */
@Data
public class OrderCreateDTO {
    
    /**
     * 物品类型: MODEL-模型, TOOL-工具
     */
    private String itemType;
    
    /**
     * 物品ID
     */
    private Long itemId;
    
    /**
     * 成交价格
     */
    private BigDecimal price;
    
    /**
     * 备注
     */
    private String remark;
}

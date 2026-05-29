package com.model.management.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelQueryDTO {
    private Integer page = 1;
    private Integer size = 20;
    private String manufacturerName;
    private String modelName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer sold;
    private Integer isPublic;
    
    /**
     * 用户ID(用于数据隔离)
     */
    private Long userId;
}

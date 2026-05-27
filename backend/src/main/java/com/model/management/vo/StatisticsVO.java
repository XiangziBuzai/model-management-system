package com.model.management.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsVO {
    private long modelCount;
    private long toolCount;
    private BigDecimal modelTotalPrice;
    private BigDecimal toolTotalPrice;
    private BigDecimal totalPrice;
    private long manufacturerCount;
}

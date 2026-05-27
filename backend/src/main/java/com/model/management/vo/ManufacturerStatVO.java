package com.model.management.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManufacturerStatVO {
    private Long manufacturerId;
    private String manufacturerName;
    private long modelCount;
    private BigDecimal totalPrice;
    private double percentage; // 数量占比(%)
}

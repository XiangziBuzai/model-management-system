package com.model.management.dto;

import lombok.Data;

/**
 * 订单发货DTO
 */
@Data
public class OrderShipDTO {
    
    /**
     * 快递单号
     */
    private String trackingNumber;
    
    /**
     * 快递公司
     */
    private String expressCompany;
}

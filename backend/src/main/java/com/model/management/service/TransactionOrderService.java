package com.model.management.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.OrderCreateDTO;
import com.model.management.dto.OrderShipDTO;
import com.model.management.entity.TransactionOrder;

/**
 * 交易订单服务接口
 */
public interface TransactionOrderService {
    
    /**
     * 创建订单
     */
    TransactionOrder createOrder(Long buyerId, OrderCreateDTO dto);
    
    /**
     * 分页查询我的购买记录
     */
    Page<TransactionOrder> getMyPurchases(Long buyerId, int pageNum, int pageSize);
    
    /**
     * 分页查询我的销售记录
     */
    Page<TransactionOrder> getMySales(Long sellerId, int pageNum, int pageSize);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, Long userId);
    
    /**
     * 发货（卖家填写快递信息）
     */
    boolean shipOrder(Long orderId, Long sellerId, OrderShipDTO dto);
    
    /**
     * 确认收货（买家确认收货）
     */
    boolean confirmReceipt(Long orderId, Long buyerId);
}

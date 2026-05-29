package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.OrderCreateDTO;
import com.model.management.dto.OrderShipDTO;
import com.model.management.entity.*;
import com.model.management.mapper.*;
import com.model.management.service.TransactionOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 交易订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionOrderServiceImpl implements TransactionOrderService {

    private final TransactionOrderMapper orderMapper;
    private final ModelMapper modelMapper;
    private final ToolMapper toolMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionOrder createOrder(Long buyerId, OrderCreateDTO dto) {
        // 验证买家是否存在
        User buyer = userMapper.selectById(buyerId);
        if (buyer == null) {
            throw new RuntimeException("买家不存在");
        }

        // 获取物品信息并验证
        Long sellerId;
        String itemName;
        
        if ("MODEL".equals(dto.getItemType())) {
            Model model = modelMapper.selectById(dto.getItemId());
            if (model == null) {
                throw new RuntimeException("模型不存在");
            }
            sellerId = model.getUserId();
            itemName = model.getName();
            
            // 不能购买自己的物品
            if (sellerId.equals(buyerId)) {
                throw new RuntimeException("无法购买自己发布的模型");
            }
        } else if ("TOOL".equals(dto.getItemType())) {
            Tool tool = toolMapper.selectById(dto.getItemId());
            if (tool == null) {
                throw new RuntimeException("工具不存在");
            }
            sellerId = tool.getUserId();
            itemName = tool.getName();
            
            // 不能购买自己的物品
            if (sellerId.equals(buyerId)) {
                throw new RuntimeException("无法购买自己发布的工具");
            }
        } else {
            throw new RuntimeException("无效的物品类型");
        }

        // 验证卖家是否存在
        User seller = userMapper.selectById(sellerId);
        if (seller == null) {
            throw new RuntimeException("卖家不存在");
        }

        // 创建订单
        TransactionOrder order = new TransactionOrder();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setItemType(dto.getItemType());
        order.setItemId(dto.getItemId());
        order.setItemName(itemName);
        order.setPrice(dto.getPrice());
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);
        
        // 更新物品的售出状态为1（已售出）
        if ("MODEL".equals(dto.getItemType())) {
            Model model = modelMapper.selectById(dto.getItemId());
            if (model != null) {
                model.setSold(1);
                modelMapper.updateById(model);
                log.info("模型 {} 已标记为售出", dto.getItemId());
            }
        } else if ("TOOL".equals(dto.getItemType())) {
            Tool tool = toolMapper.selectById(dto.getItemId());
            if (tool != null) {
                tool.setSold(1);
                toolMapper.updateById(tool);
                log.info("工具 {} 已标记为售出", dto.getItemId());
            }
        }
        
        log.info("创建订单成功，订单号: {}, 买家: {}, 卖家: {}", order.getOrderNo(), buyerId, sellerId);
        
        return order;
    }

    @Override
    public Page<TransactionOrder> getMyPurchases(Long buyerId, int pageNum, int pageSize) {
        Page<TransactionOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TransactionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionOrder::getBuyerId, buyerId)
               .orderByDesc(TransactionOrder::getCreatedAt);
        
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<TransactionOrder> getMySales(Long sellerId, int pageNum, int pageSize) {
        Page<TransactionOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TransactionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionOrder::getSellerId, sellerId)
               .orderByDesc(TransactionOrder::getCreatedAt);
        
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId) {
        TransactionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有买家或卖家可以取消订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 只能取消待发货的订单
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待发货的订单");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        
        // 订单取消后，将物品的售出状态恢复为0（未售出）
        if ("MODEL".equals(order.getItemType())) {
            Model model = modelMapper.selectById(order.getItemId());
            if (model != null) {
                model.setSold(0);
                modelMapper.updateById(model);
                log.info("模型 {} 已恢复为未售出", order.getItemId());
            }
        } else if ("TOOL".equals(order.getItemType())) {
            Tool tool = toolMapper.selectById(order.getItemId());
            if (tool != null) {
                tool.setSold(0);
                toolMapper.updateById(tool);
                log.info("工具 {} 已恢复为未售出", order.getItemId());
            }
        }
        
        log.info("订单 {} 已取消", orderId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long orderId, Long sellerId, OrderShipDTO dto) {
        TransactionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证是否是卖家
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("只有卖家可以发货");
        }

        // 只能发货待发货的订单
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能发货待发货的订单");
        }

        // 验证快递单号
        if (dto.getTrackingNumber() == null || dto.getTrackingNumber().trim().isEmpty()) {
            throw new RuntimeException("快递单号不能为空");
        }

        // 更新订单状态和物流信息
        order.setStatus("SHIPPED");
        order.setTrackingNumber(dto.getTrackingNumber());
        order.setExpressCompany(dto.getExpressCompany());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("订单 {} 已发货，快递单号: {}, 快递公司: {}", orderId, dto.getTrackingNumber(), dto.getExpressCompany());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(Long orderId, Long buyerId) {
        TransactionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证是否是买家
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("只有买家可以确认收货");
        }

        // 只能确认已发货的订单
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("只能确认已发货的订单");
        }

        // 更新订单状态为已完成
        order.setStatus("COMPLETED");
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("订单 {} 已确认收货", orderId);
        return result > 0;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}

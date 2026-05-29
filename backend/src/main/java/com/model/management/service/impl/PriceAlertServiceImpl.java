package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.model.management.entity.*;
import com.model.management.mapper.*;
import com.model.management.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格监控服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertMapper priceAlertMapper;
    private final FavoriteMapper favoriteMapper;
    private final ModelMapper modelMapper;
    private final ToolMapper toolMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkPriceChanges() {
        log.info("开始检查价格变动...");

        // 获取所有收藏记录
        List<Favorite> favorites = favoriteMapper.selectList(null);
        
        for (Favorite favorite : favorites) {
            BigDecimal currentPrice = null;
            
            // 获取当前价格
            if ("MODEL".equals(favorite.getItemType())) {
                Model model = modelMapper.selectById(favorite.getItemId());
                if (model != null) {
                    currentPrice = model.getPrice();
                }
            } else if ("TOOL".equals(favorite.getItemType())) {
                Tool tool = toolMapper.selectById(favorite.getItemId());
                if (tool != null) {
                    currentPrice = tool.getPrice();
                }
            }
            
            if (currentPrice == null) {
                continue;
            }

            // 检查是否已有价格提醒记录
            LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PriceAlert::getUserId, favorite.getUserId())
                   .eq(PriceAlert::getItemType, favorite.getItemType())
                   .eq(PriceAlert::getItemId, favorite.getItemId());
            
            PriceAlert priceAlert = priceAlertMapper.selectOne(wrapper);
            
            if (priceAlert == null) {
                // 创建新的价格提醒记录
                priceAlert = new PriceAlert();
                priceAlert.setUserId(favorite.getUserId());
                priceAlert.setItemType(favorite.getItemType());
                priceAlert.setItemId(favorite.getItemId());
                priceAlert.setLastPrice(currentPrice);
                priceAlert.setCurrentPrice(currentPrice);
                priceAlert.setIsNotified(1); // 初始状态标记为已通知，因为价格没有变动
                priceAlertMapper.insert(priceAlert);
                log.debug("为用户 {} 创建价格提醒记录，物品: {}, 价格: {}", 
                         favorite.getUserId(), favorite.getItemId(), currentPrice);
            } else {
                // 检查价格是否变化
                if (priceAlert.getCurrentPrice().compareTo(currentPrice) != 0) {
                    log.info("检测到价格变动！用户: {}, 物品类型: {}, 物品ID: {}, 原价格: {}, 新价格: {}",
                            favorite.getUserId(), favorite.getItemType(), favorite.getItemId(),
                            priceAlert.getCurrentPrice(), currentPrice);
                    
                    // 更新价格
                    priceAlert.setLastPrice(priceAlert.getCurrentPrice());
                    priceAlert.setCurrentPrice(currentPrice);
                    priceAlert.setIsNotified(0); // 标记为未通知
                    priceAlertMapper.updateById(priceAlert);
                }
            }
        }
        
        log.info("价格变动检查完成");
    }

    @Override
    public List<PriceAlert> getUnnotifiedAlerts(Long userId) {
        LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceAlert::getUserId, userId)
               .eq(PriceAlert::getIsNotified, 0)
               .orderByDesc(PriceAlert::getUpdatedAt);
        return priceAlertMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsNotified(Long userId, List<Long> alertIds) {
        for (Long alertId : alertIds) {
            PriceAlert priceAlert = priceAlertMapper.selectById(alertId);
            if (priceAlert != null && priceAlert.getUserId().equals(userId)) {
                priceAlert.setIsNotified(1);
                priceAlertMapper.updateById(priceAlert);
            }
        }
        log.info("用户 {} 的 {} 条价格提醒已标记为已通知", userId, alertIds.size());
    }
}

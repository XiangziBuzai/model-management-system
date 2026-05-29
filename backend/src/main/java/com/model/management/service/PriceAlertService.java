package com.model.management.service;

import com.model.management.entity.PriceAlert;

import java.util.List;

/**
 * 价格监控服务接口
 */
public interface PriceAlertService {
    
    /**
     * 检查并更新价格变动（由定时任务调用）
     */
    void checkPriceChanges();

    /**
     * 获取用户未通知的价格变动提醒
     * @param userId 用户ID
     * @return 未通知的价格变动列表
     */
    List<PriceAlert> getUnnotifiedAlerts(Long userId);

    /**
     * 标记提醒为已通知
     * @param userId 用户ID
     * @param alertIds 提醒ID列表
     */
    void markAsNotified(Long userId, List<Long> alertIds);
}

package com.model.management.config;

import com.model.management.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 价格监控定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceAlertTask {

    private final PriceAlertService priceAlertService;

    /**
     * 每100分钟检查一次价格变动
     */
    @Scheduled(fixedRate = 6000000) // 100分钟 = 6000000毫秒
    public void checkPriceChanges() {
        try {
            log.info("定时任务：开始执行价格变动检查");
            priceAlertService.checkPriceChanges();
        } catch (Exception e) {
            log.error("定时任务：价格变动检查失败", e);
        }
    }
}

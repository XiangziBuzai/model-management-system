package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.entity.PriceAlert;
import com.model.management.entity.Model;
import com.model.management.entity.Tool;
import com.model.management.mapper.ModelMapper;
import com.model.management.mapper.ToolMapper;
import com.model.management.service.PriceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格提醒控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/price-alert")
@RequiredArgsConstructor
@Tag(name = "价格提醒", description = "价格变动提醒相关接口")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final ModelMapper modelMapper;
    private final ToolMapper toolMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /**
     * 获取用户未通知的价格变动提醒
     */
    @Operation(summary = "获取价格变动提醒")
    @GetMapping("/alerts")
    public Result<Map<String, Object>> getPriceAlerts(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        List<PriceAlert> alerts = priceAlertService.getUnnotifiedAlerts(userId);
        
        // 获取物品详情信息
        List<Map<String, Object>> alertDetails = alerts.stream().map(alert -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", alert.getId());
            detail.put("itemType", alert.getItemType());
            detail.put("itemId", alert.getItemId());
            detail.put("lastPrice", alert.getLastPrice());
            detail.put("currentPrice", alert.getCurrentPrice());
            
            BigDecimal priceChange = alert.getCurrentPrice().subtract(alert.getLastPrice());
            detail.put("priceChange", priceChange);
            detail.put("priceChangePercent", priceChange.divide(alert.getLastPrice(), 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            
            // 获取物品名称
            if ("MODEL".equals(alert.getItemType())) {
                Model model = modelMapper.selectById(alert.getItemId());
                if (model != null) {
                    detail.put("itemName", model.getName());
                }
            } else if ("TOOL".equals(alert.getItemType())) {
                Tool tool = toolMapper.selectById(alert.getItemId());
                if (tool != null) {
                    detail.put("itemName", tool.getName());
                }
            }
            
            return detail;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("alerts", alertDetails);
        result.put("count", alertDetails.size());
        
        return Result.success(result);
    }

    /**
     * 标记提醒为已通知
     */
    @Operation(summary = "标记提醒为已通知")
    @PostMapping("/mark-notified")
    public Result<Boolean> markAsNotified(HttpServletRequest request, @RequestBody List<Long> alertIds) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        priceAlertService.markAsNotified(userId, alertIds);
        return Result.success(true);
    }

    /**
     * 手动触发价格检查（用于测试）
     */
    @Operation(summary = "手动触发价格检查")
    @PostMapping("/check")
    public Result<Boolean> checkPriceChanges(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        priceAlertService.checkPriceChanges();
        return Result.success(true);
    }
}

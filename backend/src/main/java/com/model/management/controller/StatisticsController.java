package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.service.StatisticsService;
import com.model.management.vo.ManufacturerStatVO;
import com.model.management.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "数据统计")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "总览统计（总数、总金额）")
    @GetMapping("/overview")
    public Result<StatisticsVO> overview() {
        return Result.success(statisticsService.overview());
    }

    @Operation(summary = "各厂家统计（数量、金额、占比）")
    @GetMapping("/manufacturer")
    public Result<List<ManufacturerStatVO>> manufacturerStats() {
        return Result.success(statisticsService.manufacturerStats());
    }

    @Operation(summary = "价格区间分布")
    @GetMapping("/price-distribution")
    public Result<Map<String, Long>> priceDistribution() {
        return Result.success(statisticsService.priceDistribution());
    }
}

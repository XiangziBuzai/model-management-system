package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.model.management.entity.Model;
import com.model.management.entity.Tool;
import com.model.management.mapper.ManufacturerMapper;
import com.model.management.mapper.ModelMapper;
import com.model.management.mapper.ToolMapper;
import com.model.management.service.StatisticsService;
import com.model.management.vo.ManufacturerStatVO;
import com.model.management.vo.StatisticsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ModelMapper modelMapper;
    private final ToolMapper toolMapper;
    private final ManufacturerMapper manufacturerMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute("userId");
        }
        return null;
    }

    @Override
    public StatisticsVO overview() {
        Long userId = getCurrentUserId();
        StatisticsVO vo = new StatisticsVO();

        // 模型数量(只统计当前用户的)
        LambdaQueryWrapper<Model> modelWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            modelWrapper.eq(Model::getUserId, userId);
        }
        Long modelCount = modelMapper.selectCount(modelWrapper);
        vo.setModelCount(modelCount == null ? 0 : modelCount);

        // 工具数量(只统计当前用户的)
        LambdaQueryWrapper<Tool> toolWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            toolWrapper.eq(Tool::getUserId, userId);
        }
        Long toolCount = toolMapper.selectCount(toolWrapper);
        vo.setToolCount(toolCount == null ? 0 : toolCount);

        // 厂家数量
        Long mfCount = manufacturerMapper.selectCount(null);
        vo.setManufacturerCount(mfCount == null ? 0 : mfCount);

        // 模型总金额
        List<Model> models = modelMapper.selectList(
                modelWrapper.select(Model::getPrice));
        BigDecimal modelTotal = models.stream()
                .map(Model::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setModelTotalPrice(modelTotal);

        // 工具总金额
        List<Tool> tools = toolMapper.selectList(
                toolWrapper.select(Tool::getPrice));
        BigDecimal toolTotal = tools.stream()
                .map(Tool::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setToolTotalPrice(toolTotal);

        vo.setTotalPrice(modelTotal);
        return vo;
    }

    @Override
    public List<ManufacturerStatVO> manufacturerStats() {
        Long userId = getCurrentUserId();
        List<ManufacturerStatVO> list = modelMapper.selectStatsByManufacturer(userId);
        // 计算数量占比
        long total = list.stream().mapToLong(ManufacturerStatVO::getModelCount).sum();
        if (total > 0) {
            list.forEach(s -> s.setPercentage(
                    Math.round(s.getModelCount() * 10000.0 / total) / 100.0
            ));
        }
        return list;
    }

    @Override
    public Map<String, Long> priceDistribution() {
        Long userId = getCurrentUserId();
        
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Model::getUserId, userId);
        }
        wrapper.select(Model::getPrice);
        
        List<Model> models = modelMapper.selectList(wrapper);

        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("100元以下", 0L);
        dist.put("100-200元", 0L);
        dist.put("200-300元", 0L);
        dist.put("300-500元", 0L);
        dist.put("500元以上", 0L);

        for (Model m : models) {
            if (m.getPrice() == null) continue;
            double p = m.getPrice().doubleValue();
            if (p < 100) {
                dist.merge("100元以下", 1L, Long::sum);
            } else if (p < 200) {
                dist.merge("100-200元", 1L, Long::sum);
            } else if (p < 300) {
                dist.merge("200-300元", 1L, Long::sum);
            } else if (p < 500) {
                dist.merge("300-500元", 1L, Long::sum);
            } else {
                dist.merge("500元以上", 1L, Long::sum);
            }
        }
        return dist;
    }
}

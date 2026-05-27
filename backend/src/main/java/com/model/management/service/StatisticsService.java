package com.model.management.service;

import com.model.management.vo.ManufacturerStatVO;
import com.model.management.vo.StatisticsVO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    StatisticsVO overview();
    List<ManufacturerStatVO> manufacturerStats();
    Map<String, Long> priceDistribution();
}

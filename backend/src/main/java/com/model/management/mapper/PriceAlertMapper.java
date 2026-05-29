package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model.management.entity.PriceAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格变动通知Mapper接口
 */
@Mapper
public interface PriceAlertMapper extends BaseMapper<PriceAlert> {
}

package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model.management.entity.TransactionOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易订单Mapper接口
 */
@Mapper
public interface TransactionOrderMapper extends BaseMapper<TransactionOrder> {
}

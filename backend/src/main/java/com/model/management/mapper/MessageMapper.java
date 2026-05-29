package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model.management.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私信消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

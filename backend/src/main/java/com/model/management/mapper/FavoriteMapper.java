package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model.management.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper接口
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}

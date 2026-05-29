package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.ModelQueryDTO;
import com.model.management.entity.Model;
import com.model.management.vo.ManufacturerStatVO;
import com.model.management.vo.ModelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelMapper extends BaseMapper<Model> {

    IPage<ModelVO> selectModelVOPage(Page<ModelVO> page, ModelQueryDTO dto);

    List<ManufacturerStatVO> selectStatsByManufacturer(@Param("userId") Long userId);
    
    /**
     * 查询公开模型列表（广场使用）
     */
    IPage<ModelVO> selectPublicModelVOPage(Page<ModelVO> page, @Param("keyword") String keyword, @Param("sortBy") String sortBy);
    
    /**
     * 查询公开模型详情（广场使用）
     */
    ModelVO selectPublicModelVOById(@Param("id") Long id);
    
    /**
     * 更新收藏数
     */
    void updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);
    
    /**
     * 更新浏览量（+1）
     */
    void incrementViewCount(@Param("id") Long id);

    /**
     * 查询指定用户的公开模型列表
     */
    IPage<ModelVO> selectPublicModelVOPageByUser(Page<ModelVO> page, @Param("userId") Long userId);
}

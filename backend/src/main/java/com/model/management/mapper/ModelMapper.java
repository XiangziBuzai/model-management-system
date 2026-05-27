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
}

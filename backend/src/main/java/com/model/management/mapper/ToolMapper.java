package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.entity.Tool;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    IPage<Tool> selectToolPage(Page<Tool> page, ToolQueryDTO dto);
}

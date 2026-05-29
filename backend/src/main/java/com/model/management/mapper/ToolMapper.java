package com.model.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.entity.Tool;
import com.model.management.vo.ToolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    IPage<Tool> selectToolPage(Page<Tool> page, ToolQueryDTO dto);
    
    /**
     * 查询公开工具列表（广场使用）
     */
    IPage<ToolVO> selectPublicToolPage(IPage<ToolVO> page, @Param("keyword") String keyword, @Param("sortBy") String sortBy);
    
    /**
     * 查询公开工具详情（广场使用）
     */
    ToolVO selectPublicToolVOById(Long id);
    
    /**
     * 更新收藏数
     */
    void updateFavoriteCount(Long id, int delta);
    
    /**
     * 更新浏览量（+1）
     */
    void incrementViewCount(Long id);

    /**
     * 查询指定用户的公开工具列表
     */
    IPage<ToolVO> selectPublicToolPageByUser(IPage<ToolVO> page, @Param("userId") Long userId);
}

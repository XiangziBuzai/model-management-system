package com.model.management.service;

import com.model.management.common.PageResult;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.dto.ToolSaveDTO;
import com.model.management.entity.Tool;
import com.model.management.vo.ToolVO;

public interface ToolService {
    PageResult<Tool> page(ToolQueryDTO dto);
    Tool getById(Long id);
    Tool create(ToolSaveDTO dto);
    Tool update(Long id, ToolSaveDTO dto);
    boolean delete(Long id);
    
    /**
     * 分页查询公开工具列表（广场使用）
     * @param sortBy 排序方式：newest-最新（默认），hot-最热
     */
    PageResult<ToolVO> getPublicTools(int pageNum, int pageSize, String keyword, String sortBy);
    
    /**
     * 获取公开工具详情（广场使用）
     */
    ToolVO getPublicToolById(Long id);
    
    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);

    /**
     * 分页查询指定用户的公开工具列表
     */
    PageResult<ToolVO> getPublicToolsByUser(Long userId, int pageNum, int pageSize);
}

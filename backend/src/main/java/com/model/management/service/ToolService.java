package com.model.management.service;

import com.model.management.common.PageResult;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.dto.ToolSaveDTO;
import com.model.management.entity.Tool;

public interface ToolService {
    PageResult<Tool> page(ToolQueryDTO dto);
    Tool getById(Long id);
    Tool create(ToolSaveDTO dto);
    Tool update(Long id, ToolSaveDTO dto);
    boolean delete(Long id);
}

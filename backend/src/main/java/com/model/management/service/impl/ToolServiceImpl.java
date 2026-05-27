package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.common.PageResult;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.dto.ToolSaveDTO;
import com.model.management.entity.Tool;
import com.model.management.mapper.ToolMapper;
import com.model.management.service.ToolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final ToolMapper toolMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute("userId");
        }
        return null;
    }

    @Override
    public PageResult<Tool> page(ToolQueryDTO dto) {
        // 设置当前用户ID，只查询该用户的数据
        Long userId = getCurrentUserId();
        if (userId != null) {
            dto.setUserId(userId);
        }
        
        Page<Tool> page = new Page<>(dto.getPage(), dto.getSize());
        toolMapper.selectToolPage(page, dto);
        return PageResult.of(page);
    }

    @Override
    public Tool getById(Long id) {
        Long userId = getCurrentUserId();
        return toolMapper.selectOne(
                new LambdaQueryWrapper<Tool>()
                        .eq(Tool::getId, id)
                        .eq(userId != null, Tool::getUserId, userId)
        );
    }

    @Override
    public Tool create(ToolSaveDTO dto) {
        Long userId = getCurrentUserId();
        Tool tool = new Tool();
        tool.setName(dto.getName().trim());
        tool.setPrice(dto.getPrice());
        tool.setRemark(dto.getRemark());
        tool.setSold(dto.getSold() != null ? dto.getSold() : 0);
        tool.setUserId(userId); // 设置用户ID
        toolMapper.insert(tool);
        return tool;
    }

    @Override
    public Tool update(Long id, ToolSaveDTO dto) {
        Long userId = getCurrentUserId();
        Tool tool = toolMapper.selectOne(
                new LambdaQueryWrapper<Tool>()
                        .eq(Tool::getId, id)
                        .eq(userId != null, Tool::getUserId, userId)
        );
        if (tool == null) throw new RuntimeException("工具不存在或无权限操作");
        tool.setName(dto.getName().trim());
        tool.setPrice(dto.getPrice());
        tool.setRemark(dto.getRemark());
        tool.setSold(dto.getSold() != null ? dto.getSold() : 0);
        toolMapper.updateById(tool);
        return tool;
    }

    @Override
    public boolean delete(Long id) {
        Long userId = getCurrentUserId();
        Tool tool = toolMapper.selectOne(
                new LambdaQueryWrapper<Tool>()
                        .eq(Tool::getId, id)
                        .eq(userId != null, Tool::getUserId, userId)
        );
        if (tool == null) return false;
        return toolMapper.deleteById(id) > 0;
    }
}

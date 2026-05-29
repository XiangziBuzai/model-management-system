package com.model.management.controller;

import com.model.management.common.PageResult;
import com.model.management.common.Result;
import com.model.management.dto.ToolQueryDTO;
import com.model.management.dto.ToolSaveDTO;
import com.model.management.entity.Tool;
import com.model.management.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工具管理")
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @Operation(summary = "分页查询工具列表")
    @GetMapping
    public Result<PageResult<Tool>> page(ToolQueryDTO dto) {
        return Result.success(toolService.page(dto));
    }

    @Operation(summary = "查询单个工具")
    @GetMapping("/{id}")
    public Result<Tool> getById(@PathVariable Long id) {
        Tool tool = toolService.getById(id);
        if (tool == null) return Result.error(404, "工具不存在");
        return Result.success(tool);
    }

    @Operation(summary = "新增工具")
    @PostMapping
    public Result<Tool> create(@Valid @RequestBody ToolSaveDTO dto) {
        return Result.success(toolService.create(dto));
    }

    @Operation(summary = "编辑工具")
    @PutMapping("/{id}")
    public Result<Tool> update(@PathVariable Long id,
                                @Valid @RequestBody ToolSaveDTO dto) {
        return Result.success(toolService.update(id, dto));
    }

    @Operation(summary = "删除工具")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(toolService.delete(id));
    }

    @Operation(summary = "批量设置工具为公开")
    @PutMapping("/batch/public")
    public Result<Boolean> batchSetPublic(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要设为公开的工具");
        }
        return Result.success(toolService.batchSetPublic(ids));
    }

    @Operation(summary = "批量设置工具为私有")
    @PutMapping("/batch/private")
    public Result<Boolean> batchSetPrivate(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要设为私有的工具");
        }
        return Result.success(toolService.batchSetPrivate(ids));
    }

    @Operation(summary = "设置当前用户所有工具为公开")
    @PutMapping("/all/public")
    public Result<Boolean> setAllPublic() {
        return Result.success(toolService.setAllPublic());
    }

    @Operation(summary = "设置当前用户所有工具为私有")
    @PutMapping("/all/private")
    public Result<Boolean> setAllPrivate() {
        return Result.success(toolService.setAllPrivate());
    }
}

package com.model.management.controller;

import com.model.management.common.PageResult;
import com.model.management.common.Result;
import com.model.management.dto.ModelQueryDTO;
import com.model.management.dto.ModelSaveDTO;
import com.model.management.service.ModelService;
import com.model.management.vo.ModelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模型管理")
@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "分页查询模型列表")
    @GetMapping
    public Result<PageResult<ModelVO>> page(ModelQueryDTO dto) {
        log.info("查询模型列表, 参数: {}", dto);
        return Result.success(modelService.page(dto));
    }

    @Operation(summary = "查询单个模型")
    @GetMapping("/{id}")
    public Result<ModelVO> getById(@PathVariable Long id) {
        ModelVO vo = modelService.getById(id);
        if (vo == null) return Result.error(404, "模型不存在");
        return Result.success(vo);
    }

    @Operation(summary = "新增模型")
    @PostMapping
    public Result<ModelVO> create(@Valid @RequestBody ModelSaveDTO dto) {
        return Result.success(modelService.create(dto));
    }

    @Operation(summary = "编辑模型")
    @PutMapping("/{id}")
    public Result<ModelVO> update(@PathVariable Long id,
                                   @Valid @RequestBody ModelSaveDTO dto) {
        return Result.success(modelService.update(id, dto));
    }

    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(modelService.delete(id));
    }

    @Operation(summary = "批量删除模型")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的模型");
        }
        return Result.success(modelService.batchDelete(ids));
    }

    @Operation(summary = "批量设置模型为公开")
    @PutMapping("/batch/public")
    public Result<Boolean> batchSetPublic(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要设为公开的模型");
        }
        return Result.success(modelService.batchSetPublic(ids));
    }

    @Operation(summary = "批量设置模型为私有")
    @PutMapping("/batch/private")
    public Result<Boolean> batchSetPrivate(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要设为私有的模型");
        }
        return Result.success(modelService.batchSetPrivate(ids));
    }

    @Operation(summary = "设置当前用户所有模型为公开")
    @PutMapping("/all/public")
    public Result<Boolean> setAllPublic() {
        return Result.success(modelService.setAllPublic());
    }

    @Operation(summary = "设置当前用户所有模型为私有")
    @PutMapping("/all/private")
    public Result<Boolean> setAllPrivate() {
        return Result.success(modelService.setAllPrivate());
    }
}

package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.entity.Manufacturer;
import com.model.management.service.ManufacturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "厂家管理")
@RestController
@RequestMapping("/api/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @Operation(summary = "获取全部厂家列表")
    @GetMapping
    public Result<List<Manufacturer>> listAll() {
        return Result.success(manufacturerService.listAll());
    }

    @Operation(summary = "新增厂家")
    @PostMapping
    public Result<Manufacturer> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        if (name == null || name.isBlank()) {
            return Result.error(400, "厂家名称不能为空");
        }
        return Result.success(manufacturerService.create(name, description));
    }

    @Operation(summary = "编辑厂家")
    @PutMapping("/{id}")
    public Result<Manufacturer> update(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        if (name == null || name.isBlank()) {
            return Result.error(400, "厂家名称不能为空");
        }
        return Result.success(manufacturerService.update(id, name, description));
    }

    @Operation(summary = "删除厂家")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(manufacturerService.delete(id));
    }
}

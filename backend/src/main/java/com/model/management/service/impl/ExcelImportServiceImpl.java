package com.model.management.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.model.management.entity.Manufacturer;
import com.model.management.entity.Model;
import com.model.management.entity.Tool;
import com.model.management.excel.ExcelModelRow;
import com.model.management.excel.ExcelToolRow;
import com.model.management.mapper.ManufacturerMapper;
import com.model.management.mapper.ModelMapper;
import com.model.management.mapper.ToolMapper;
import com.model.management.service.ExcelImportService;
import com.model.management.vo.ImportResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    private final ManufacturerMapper manufacturerMapper;
    private final ModelMapper modelMapper;
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
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importModels(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        ImportResultVO result = new ImportResultVO();
        
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或登录已过期");
        }
        log.info("当前用户ID: {}", userId);
        
        AtomicInteger rowIndex = new AtomicInteger(2); // 从第2行开始（第1行为表头）

        List<ExcelModelRow> rows = new ArrayList<>();
        try {
            log.info("开始读取 Excel 文件: {}", file.getOriginalFilename());
            EasyExcel.read(file.getInputStream(), ExcelModelRow.class,
                    new AnalysisEventListener<ExcelModelRow>() {
                        @Override
                        public void invoke(ExcelModelRow row, AnalysisContext ctx) {
                            log.info("读取到一行数据: {}", row);
                            rows.add(row);
                        }
                        @Override
                        public void doAfterAllAnalysed(AnalysisContext ctx) {
                            log.info("Excel 文件读取完成，共读取 {} 行", rows.size());
                        }
                    }).sheet(0).headRowNumber(1).doRead();
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }

        log.info("Excel 解析完成，总行数: {}", rows.size());
        
        // 设置总行数
        result.setTotalRows(rows.size());

        if (rows.isEmpty()) {
            log.warn("Excel 文件中没有数据行");
            result.setDuration(System.currentTimeMillis() - startTime);
            return result;
        }

        // 批量处理（每500条一批）
        int batchSize = 500;
        for (int i = 0; i < rows.size(); i += batchSize) {
            List<ExcelModelRow> batch = rows.subList(i, Math.min(i + batchSize, rows.size()));
            processModelBatch(batch, i + 2, result, userId);
        }
        
        long endTime = System.currentTimeMillis();
        result.setDuration(endTime - startTime);
        log.info("模型导入完成，总行数: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                result.getTotalRows(), result.getSuccessCount(), result.getFailCount(), result.getDuration());
        return result;
    }

    private void processModelBatch(List<ExcelModelRow> batch, int startRow, ImportResultVO result, Long userId) {
        // 1. 数据校验 & 分离有效/无效行
        List<ExcelModelRow> validRows = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            ExcelModelRow row = batch.get(i);
            int rowNum = startRow + i;
            if (StringUtils.isBlank(row.getManufacturerName())) {
                result.addError(rowNum, "厂家名称不能为空");
                continue;
            }
            if (StringUtils.isBlank(row.getModelName())) {
                result.addError(rowNum, "模型名称不能为空");
                continue;
            }
            if (row.getPrice() == null || row.getPrice().doubleValue() < 0) {
                result.addError(rowNum, "价格无效（必须为非负数）");
                continue;
            }
            validRows.add(row);
        }
        if (validRows.isEmpty()) return;

        log.info("开始处理模型批次，有效行数: {}", validRows.size());

        // 2. 查询/创建厂家，构建 name->id 映射
        Set<String> mfNames = validRows.stream()
                .map(r -> r.getManufacturerName().trim())
                .collect(Collectors.toSet());

        Map<String, Long> mfMap = new HashMap<>();
        // 查询已存在厂家
        List<Manufacturer> existing = manufacturerMapper.selectList(
                new LambdaQueryWrapper<Manufacturer>().in(Manufacturer::getName, mfNames));
        existing.forEach(m -> mfMap.put(m.getName(), m.getId()));

        // 新增不存在的厂家
        for (String mfName : mfNames) {
            if (!mfMap.containsKey(mfName)) {
                Manufacturer mf = new Manufacturer();
                mf.setName(mfName);
                manufacturerMapper.insert(mf);
                mfMap.put(mfName, mf.getId());
                log.info("创建新厂家: {}", mfName);
            }
        }

        // 3. 构建 Model 列表并 upsert
        for (ExcelModelRow row : validRows) {
            Long mfId = mfMap.get(row.getManufacturerName().trim());
            // 按 (manufacturer_id, name, user_id) 查重
            Long count = modelMapper.selectCount(
                    new LambdaQueryWrapper<Model>()
                            .eq(Model::getManufacturerId, mfId)
                            .eq(Model::getName, row.getModelName().trim())
                            .eq(Model::getUserId, userId)
            );
            if (count > 0) {
                // 更新价格和备注
                Model update = new Model();
                update.setPrice(row.getPrice());
                update.setRemark(row.getRemark());
                modelMapper.update(update,
                        new LambdaQueryWrapper<Model>()
                                .eq(Model::getManufacturerId, mfId)
                                .eq(Model::getName, row.getModelName().trim())
                                .eq(Model::getUserId, userId)
                );
                log.info("更新模型: {} (厂家ID: {})", row.getModelName(), mfId);
            } else {
                Model model = new Model();
                model.setManufacturerId(mfId);
                model.setName(row.getModelName().trim());
                model.setPrice(row.getPrice());
                model.setRemark(row.getRemark());
                model.setUserId(userId); // 设置用户ID
                modelMapper.insert(model);
                log.info("插入新模型: {} (厂家ID: {}, 用户ID: {})", row.getModelName(), mfId, userId);
            }
            result.setSuccessCount(result.getSuccessCount() + 1);
        }
        
        log.info("批次处理完成，当前成功计数: {}", result.getSuccessCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importTools(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        ImportResultVO result = new ImportResultVO();
        
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或登录已过期");
        }
        log.info("当前用户ID: {}", userId);
        
        List<ExcelToolRow> rows = new ArrayList<>();
        try {
            log.info("开始读取工具 Excel 文件: {}", file.getOriginalFilename());
            EasyExcel.read(file.getInputStream(), ExcelToolRow.class,
                    new AnalysisEventListener<ExcelToolRow>() {
                        @Override
                        public void invoke(ExcelToolRow row, AnalysisContext ctx) {
                            log.info("读取到一行工具数据: {}", row);
                            rows.add(row);
                        }
                        @Override
                        public void doAfterAllAnalysed(AnalysisContext ctx) {
                            log.info("工具 Excel 文件读取完成，共读取 {} 行", rows.size());
                        }
                    }).sheet(0).headRowNumber(1).doRead();
        } catch (IOException e) {
            log.error("读取工具文件失败", e);
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }

        log.info("工具 Excel 解析完成，总行数: {}", rows.size());
        
        // 设置总行数
        result.setTotalRows(rows.size());

        if (rows.isEmpty()) {
            log.warn("工具 Excel 文件中没有数据行");
            result.setDuration(System.currentTimeMillis() - startTime);
            return result;
        }

        int rowIndex = 2;
        for (ExcelToolRow row : rows) {
            if (StringUtils.isBlank(row.getName())) {
                result.addError(rowIndex, "工具名称不能为空");
                rowIndex++;
                continue;
            }
            if (row.getPrice() == null || row.getPrice().doubleValue() < 0) {
                result.addError(rowIndex, "价格无效（必须为非负数）");
                rowIndex++;
                continue;
            }
            
            try {
                // upsert - 按名称和用户ID查重
                Long count = toolMapper.selectCount(
                        new LambdaQueryWrapper<Tool>()
                                .eq(Tool::getName, row.getName().trim())
                                .eq(Tool::getUserId, userId));
                if (count > 0) {
                    Tool update = new Tool();
                    update.setPrice(row.getPrice());
                    update.setRemark(row.getRemark());
                    toolMapper.update(update,
                            new LambdaQueryWrapper<Tool>()
                                    .eq(Tool::getName, row.getName().trim())
                                    .eq(Tool::getUserId, userId));
                    log.info("更新工具: {}", row.getName());
                } else {
                    Tool tool = new Tool();
                    tool.setName(row.getName().trim());
                    tool.setPrice(row.getPrice());
                    tool.setRemark(row.getRemark());
                    tool.setUserId(userId); // 设置用户ID
                    toolMapper.insert(tool);
                    log.info("插入新工具: {} (用户ID: {})", row.getName(), userId);
                }
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                // 如果是唯一键冲突，说明Excel中有重复数据，转为更新
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    log.warn("工具 {} 重复，转为更新", row.getName());
                    Tool update = new Tool();
                    update.setPrice(row.getPrice());
                    update.setRemark(row.getRemark());
                    toolMapper.update(update,
                            new LambdaQueryWrapper<Tool>()
                                    .eq(Tool::getName, row.getName().trim())
                                    .eq(Tool::getUserId, userId));
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else {
                    result.addError(rowIndex, "导入失败：" + e.getMessage());
                    log.error("导入工具失败: {}", row.getName(), e);
                }
            }
            rowIndex++;
        }
        
        long endTime = System.currentTimeMillis();
        result.setDuration(endTime - startTime);
        log.info("工具导入完成，总行数: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                result.getTotalRows(), result.getSuccessCount(), result.getFailCount(), result.getDuration());
        return result;
    }
}

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importModels(MultipartFile file) {
        ImportResultVO result = new ImportResultVO();
        AtomicInteger rowIndex = new AtomicInteger(2); // 从第2行开始（第1行为表头）

        List<ExcelModelRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), ExcelModelRow.class,
                    new AnalysisEventListener<ExcelModelRow>() {
                        @Override
                        public void invoke(ExcelModelRow row, AnalysisContext ctx) {
                            rows.add(row);
                        }
                        @Override
                        public void doAfterAllAnalysed(AnalysisContext ctx) {}
                    }).sheet(0).headRowNumber(1).doRead();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }

        // 批量处理（每500条一批）
        int batchSize = 500;
        for (int i = 0; i < rows.size(); i += batchSize) {
            List<ExcelModelRow> batch = rows.subList(i, Math.min(i + batchSize, rows.size()));
            processModelBatch(batch, i + 2, result);
        }
        return result;
    }

    private void processModelBatch(List<ExcelModelRow> batch, int startRow, ImportResultVO result) {
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
            }
        }

        // 3. 构建 Model 列表并 upsert
        for (ExcelModelRow row : validRows) {
            Long mfId = mfMap.get(row.getManufacturerName().trim());
            // 按 (manufacturer_id, name) 查重
            Long count = modelMapper.selectCount(
                    new LambdaQueryWrapper<Model>()
                            .eq(Model::getManufacturerId, mfId)
                            .eq(Model::getName, row.getModelName().trim())
            );
            if (count > 0) {
                // 更新价格
                Model update = new Model();
                update.setPrice(row.getPrice());
                update.setRemark(row.getRemark());
                modelMapper.update(update,
                        new LambdaQueryWrapper<Model>()
                                .eq(Model::getManufacturerId, mfId)
                                .eq(Model::getName, row.getModelName().trim())
                );
            } else {
                Model model = new Model();
                model.setManufacturerId(mfId);
                model.setName(row.getModelName().trim());
                model.setPrice(row.getPrice());
                model.setRemark(row.getRemark());
                modelMapper.insert(model);
            }
            result.setSuccessCount(result.getSuccessCount() + 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importTools(MultipartFile file) {
        ImportResultVO result = new ImportResultVO();
        List<ExcelToolRow> rows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), ExcelToolRow.class,
                    new AnalysisEventListener<ExcelToolRow>() {
                        @Override
                        public void invoke(ExcelToolRow row, AnalysisContext ctx) {
                            rows.add(row);
                        }
                        @Override
                        public void doAfterAllAnalysed(AnalysisContext ctx) {}
                    }).sheet(0).headRowNumber(1).doRead();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
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
            // upsert
            Long count = toolMapper.selectCount(
                    new LambdaQueryWrapper<Tool>().eq(Tool::getName, row.getName().trim()));
            if (count > 0) {
                Tool update = new Tool();
                update.setPrice(row.getPrice());
                update.setRemark(row.getRemark());
                toolMapper.update(update,
                        new LambdaQueryWrapper<Tool>().eq(Tool::getName, row.getName().trim()));
            } else {
                Tool tool = new Tool();
                tool.setName(row.getName().trim());
                tool.setPrice(row.getPrice());
                tool.setRemark(row.getRemark());
                toolMapper.insert(tool);
            }
            result.setSuccessCount(result.getSuccessCount() + 1);
            rowIndex++;
        }
        return result;
    }
}

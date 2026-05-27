package com.model.management.controller;

import com.alibaba.excel.EasyExcel;
import com.model.management.common.Result;
import com.model.management.excel.ExcelModelRow;
import com.model.management.excel.ExcelToolRow;
import com.model.management.service.ExcelImportService;
import com.model.management.vo.ImportResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Excel 导入")
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    @Operation(summary = "上传 Excel 导入数据", description = "type=model 导入模型，type=tool 导入工具")
    @PostMapping("/excel")
    public Result<ImportResultVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "model") String type) {

        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error(400, "仅支持 .xlsx 或 .xls 格式");
        }

        ImportResultVO result;
        if ("tool".equals(type)) {
            result = excelImportService.importTools(file);
        } else {
            result = excelImportService.importModels(file);
        }
        return Result.success(result);
    }

    @Operation(summary = "下载模型导入模板")
    @GetMapping("/template/model")
    public void downloadModelTemplate(HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("模型导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // 生成示例数据
        List<ExcelModelRow> data = new ArrayList<>();
        ExcelModelRow demo = new ExcelModelRow();
        demo.setManufacturerName("摩动核");
        demo.setModelName("示例模型");
        demo.setPrice(new java.math.BigDecimal("99.99"));
        demo.setRemark("示例备注");
        data.add(demo);

        EasyExcel.write(response.getOutputStream(), ExcelModelRow.class)
                .sheet("模型数据")
                .doWrite(data);
    }

    @Operation(summary = "下载工具导入模板")
    @GetMapping("/template/tool")
    public void downloadToolTemplate(HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("工具导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<ExcelToolRow> data = new ArrayList<>();
        ExcelToolRow demo = new ExcelToolRow();
        demo.setName("示例工具");
        demo.setPrice(new java.math.BigDecimal("29.99"));
        demo.setRemark("示例备注");
        data.add(demo);

        EasyExcel.write(response.getOutputStream(), ExcelToolRow.class)
                .sheet("工具数据")
                .doWrite(data);
    }
}

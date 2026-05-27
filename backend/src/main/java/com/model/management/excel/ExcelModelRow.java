package com.model.management.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Excel 模型导入行 - 对应模板列：厂家名称 | 模型名称 | 价格 | 备注
 */
@Data
public class ExcelModelRow {

    @ExcelProperty("厂家名称")
    private String manufacturerName;

    @ExcelProperty("模型名称")
    private String modelName;

    @ExcelProperty("价格")
    private BigDecimal price;

    @ExcelProperty("备注")
    private String remark;
}

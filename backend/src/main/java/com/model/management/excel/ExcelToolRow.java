package com.model.management.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Excel 工具导入行 - 对应模板列：工具名称 | 价格 | 备注
 */
@Data
public class ExcelToolRow {

    @ExcelProperty("工具名称")
    private String name;

    @ExcelProperty("价格")
    private BigDecimal price;

    @ExcelProperty("备注")
    private String remark;
}

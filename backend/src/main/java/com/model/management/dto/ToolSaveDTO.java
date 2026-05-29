package com.model.management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ToolSaveDTO {

    @NotBlank(message = "工具名称不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负数")
    private BigDecimal price;

    private String remark;

    private String cover;

    private Integer sold;

    private Integer isPublic;
}

package com.model.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("model")
public class Model {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long manufacturerId;

    private String name;

    private BigDecimal price;

    private String remark;

    private Integer sold;

    /**
     * 用户ID(数据归属)
     */
    private Long userId;

    /**
     * 是否公开: 0-私有, 1-公开
     */
    private Integer isPublic;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 封面图片URL
     */
    private String cover;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

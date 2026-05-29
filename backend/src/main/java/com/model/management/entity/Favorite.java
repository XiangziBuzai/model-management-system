package com.model.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@TableName("favorite")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品类型: MODEL-模型, TOOL-工具
     */
    private String itemType;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 收藏时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

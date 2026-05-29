package com.model.management.dto;

import lombok.Data;

/**
 * 收藏请求DTO
 */
@Data
public class FavoriteRequestDTO {
    
    /**
     * 物品类型: MODEL-模型, TOOL-工具
     */
    private String itemType;
    
    /**
     * 物品ID
     */
    private Long itemId;
}

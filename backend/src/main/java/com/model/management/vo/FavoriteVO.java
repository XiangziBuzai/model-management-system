package com.model.management.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏VO（包含物品信息）
 */
@Data
public class FavoriteVO {
    
    private Long id;
    
    private Long userId;
    
    private String itemType;
    
    private Long itemId;
    
    /**
     * 物品名称
     */
    private String itemName;
    
    /**
     * 物品价格
     */
    private String itemPrice;
    
    /**
     * 卖家昵称
     */
    private String sellerNickname;
    
    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 封面图片URL
     */
    private String cover;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
}

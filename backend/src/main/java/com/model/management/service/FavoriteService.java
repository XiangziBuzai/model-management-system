package com.model.management.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.FavoriteRequestDTO;
import com.model.management.vo.FavoriteVO;

/**
 * 收藏服务接口
 */
public interface FavoriteService {
    
    /**
     * 添加收藏
     */
    boolean addFavorite(Long userId, FavoriteRequestDTO dto);
    
    /**
     * 取消收藏
     */
    boolean removeFavorite(Long userId, String itemType, Long itemId);
    
    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, String itemType, Long itemId);
    
    /**
     * 分页查询我的收藏列表
     */
    Page<FavoriteVO> getMyFavorites(Long userId, int pageNum, int pageSize);

    /**
     * 分页查询指定用户的公开收藏列表
     */
    Page<FavoriteVO> getUserPublicFavorites(Long userId, int pageNum, int pageSize);
}

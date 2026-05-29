package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.FavoriteRequestDTO;
import com.model.management.entity.Favorite;
import com.model.management.entity.Model;
import com.model.management.entity.Tool;
import com.model.management.entity.User;
import com.model.management.mapper.FavoriteMapper;
import com.model.management.mapper.ModelMapper;
import com.model.management.mapper.ToolMapper;
import com.model.management.mapper.UserMapper;
import com.model.management.service.FavoriteService;
import com.model.management.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 收藏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ModelMapper modelMapper;
    private final ToolMapper toolMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFavorite(Long userId, FavoriteRequestDTO dto) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getItemType, dto.getItemType())
               .eq(Favorite::getItemId, dto.getItemId());
        
        if (favoriteMapper.selectCount(wrapper) > 0) {
            log.warn("用户 {} 已经收藏了 {} 类型的物品 {}", userId, dto.getItemType(), dto.getItemId());
            return false;
        }

        // 验证物品是否存在
        if ("MODEL".equals(dto.getItemType())) {
            Model model = modelMapper.selectById(dto.getItemId());
            if (model == null) {
                throw new RuntimeException("模型不存在");
            }
        } else if ("TOOL".equals(dto.getItemType())) {
            Tool tool = toolMapper.selectById(dto.getItemId());
            if (tool == null) {
                throw new RuntimeException("工具不存在");
            }
        } else {
            throw new RuntimeException("无效的物品类型");
        }

        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemType(dto.getItemType());
        favorite.setItemId(dto.getItemId());
        
        int result = favoriteMapper.insert(favorite);
        
        // 更新物品的收藏数
        if (result > 0) {
            if ("MODEL".equals(dto.getItemType())) {
                modelMapper.updateFavoriteCount(dto.getItemId(), 1);
            } else if ("TOOL".equals(dto.getItemType())) {
                toolMapper.updateFavoriteCount(dto.getItemId(), 1);
            }
        }
        
        log.info("用户 {} 成功收藏了 {} 类型的物品 {}", userId, dto.getItemType(), dto.getItemId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Long userId, String itemType, Long itemId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getItemType, itemType)
               .eq(Favorite::getItemId, itemId);
        
        int result = favoriteMapper.delete(wrapper);
        
        // 更新物品的收藏数
        if (result > 0) {
            if ("MODEL".equals(itemType)) {
                modelMapper.updateFavoriteCount(itemId, -1);
            } else if ("TOOL".equals(itemType)) {
                toolMapper.updateFavoriteCount(itemId, -1);
            }
        }
        
        log.info("用户 {} 取消了收藏 {} 类型的物品 {}", userId, itemType, itemId);
        return result > 0;
    }

    @Override
    public boolean isFavorited(Long userId, String itemType, Long itemId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getItemType, itemType)
               .eq(Favorite::getItemId, itemId);
        
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Page<FavoriteVO> getMyFavorites(Long userId, int pageNum, int pageSize) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreatedAt);
        
        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, wrapper);
        
        // 转换为VO并填充物品信息
        Page<FavoriteVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(favoritePage.getTotal());
        
        java.util.List<FavoriteVO> voList = new java.util.ArrayList<>();
        for (Favorite favorite : favoritePage.getRecords()) {
            FavoriteVO vo = convertToVO(favorite);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public Page<FavoriteVO> getUserPublicFavorites(Long userId, int pageNum, int pageSize) {
        // 检查用户是否设置了公开收藏列表
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getIsPublicFavorite() == null || user.getIsPublicFavorite() != 1) {
            throw new RuntimeException("该用户未公开收藏列表");
        }
        
        return getMyFavorites(userId, pageNum, pageSize);
    }

    private FavoriteVO convertToVO(Favorite favorite) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getId());
        vo.setUserId(favorite.getUserId());
        vo.setItemType(favorite.getItemType());
        vo.setItemId(favorite.getItemId());
        vo.setCreatedAt(favorite.getCreatedAt());

        // 获取物品信息
        if ("MODEL".equals(favorite.getItemType())) {
            Model model = modelMapper.selectById(favorite.getItemId());
            if (model != null) {
                vo.setItemName(model.getName());
                vo.setItemPrice(model.getPrice() != null ? model.getPrice().toString() : "0");
                vo.setCover(model.getCover());
                vo.setViewCount(model.getViewCount());
                vo.setFavoriteCount(model.getFavoriteCount());
                
                // 获取卖家信息
                User seller = userMapper.selectById(model.getUserId());
                if (seller != null) {
                    vo.setSellerNickname(seller.getNickname());
                }
            }
        } else if ("TOOL".equals(favorite.getItemType())) {
            Tool tool = toolMapper.selectById(favorite.getItemId());
            if (tool != null) {
                vo.setItemName(tool.getName());
                vo.setItemPrice(tool.getPrice() != null ? tool.getPrice().toString() : "0");
                vo.setCover(tool.getCover());
                vo.setViewCount(tool.getViewCount());
                vo.setFavoriteCount(tool.getFavoriteCount());
                
                // 获取卖家信息
                User seller = userMapper.selectById(tool.getUserId());
                if (seller != null) {
                    vo.setSellerNickname(seller.getNickname());
                }
            }
        }

        return vo;
    }
}

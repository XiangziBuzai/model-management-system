package com.model.management.controller;

import com.model.management.common.PageResult;
import com.model.management.common.Result;
import com.model.management.dto.FavoriteRequestDTO;
import com.model.management.dto.MessageSendDTO;
import com.model.management.dto.OrderCreateDTO;
import com.model.management.dto.OrderShipDTO;
import com.model.management.entity.Message;
import com.model.management.entity.TransactionOrder;
import com.model.management.service.FavoriteService;
import com.model.management.service.MessageService;
import com.model.management.service.ModelService;
import com.model.management.service.ToolService;
import com.model.management.service.TransactionOrderService;
import com.model.management.vo.FavoriteVO;
import com.model.management.vo.MessageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型广场控制器
 */
@Tag(name = "模型广场")
@Slf4j
@RestController
@RequestMapping("/api/square")
@RequiredArgsConstructor
public class SquareController {

    private final ModelService modelService;
    private final ToolService toolService;
    private final FavoriteService favoriteService;
    private final MessageService messageService;
    private final TransactionOrderService transactionOrderService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @Operation(summary = "获取公开模型列表")
    @GetMapping("/models")
    public Result<PageResult<?>> getPublicModels(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(required = false) List<Long> manufacturerIds) {
        log.info("查询公开模型列表, 页码: {}, 每页数量: {}, 关键词: {}, 排序: {}, 厂家ID列表: {}", pageNum, pageSize, keyword, sortBy, manufacturerIds);
        return Result.success(modelService.getPublicModels(pageNum, pageSize, keyword, sortBy, manufacturerIds));
    }

    @Operation(summary = "获取公开工具列表")
    @GetMapping("/tools")
    public Result<PageResult<?>> getPublicTools(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        log.info("查询公开工具列表, 页码: {}, 每页数量: {}, 关键词: {}, 排序: {}", pageNum, pageSize, keyword, sortBy);
        return Result.success(toolService.getPublicTools(pageNum, pageSize, keyword, sortBy));
    }

    @Operation(summary = "获取指定用户的公开模型列表")
    @GetMapping("/user/{userId}/models")
    public Result<PageResult<?>> getUserPublicModels(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("查询用户 {} 的公开模型列表, 页码: {}, 每页数量: {}", userId, pageNum, pageSize);
        return Result.success(modelService.getPublicModelsByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取指定用户的公开工具列表")
    @GetMapping("/user/{userId}/tools")
    public Result<PageResult<?>> getUserPublicTools(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("查询用户 {} 的公开工具列表, 页码: {}, 每页数量: {}", userId, pageNum, pageSize);
        return Result.success(toolService.getPublicToolsByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取公开模型详情")
    @GetMapping("/model/{id}")
    public Result<?> getPublicModelById(@PathVariable Long id) {
        log.info("查询公开模型详情, ID: {}", id);
        // 先增加浏览量，再查询详情
        modelService.incrementViewCount(id);
        Object model = modelService.getPublicModelById(id);
        if (model == null) {
            return Result.error(404, "模型不存在或未公开");
        }
        return Result.success(model);
    }

    @Operation(summary = "获取公开工具详情")
    @GetMapping("/tool/{id}")
    public Result<?> getPublicToolById(@PathVariable Long id) {
        log.info("查询公开工具详情, ID: {}", id);
        // 先增加浏览量，再查询详情
        toolService.incrementViewCount(id);
        Object tool = toolService.getPublicToolById(id);
        if (tool == null) {
            return Result.error(404, "工具不存在或未公开");
        }
        return Result.success(tool);
    }

    @Operation(summary = "添加收藏")
    @PostMapping("/favorite")
    public Result<Boolean> addFavorite(HttpServletRequest request, @RequestBody FavoriteRequestDTO dto) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 添加收藏, 物品类型: {}, 物品ID: {}", userId, dto.getItemType(), dto.getItemId());
        boolean success = favoriteService.addFavorite(userId, dto);
        return Result.success(success);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite")
    public Result<Boolean> removeFavorite(HttpServletRequest request,
                                          @RequestParam String itemType,
                                          @RequestParam Long itemId) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 取消收藏, 物品类型: {}, 物品ID: {}", userId, itemType, itemId);
        boolean success = favoriteService.removeFavorite(userId, itemType, itemId);
        return Result.success(success);
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/favorite/check")
    public Result<Boolean> checkFavorite(HttpServletRequest request,
                                         @RequestParam String itemType,
                                         @RequestParam Long itemId) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean favorited = favoriteService.isFavorited(userId, itemType, itemId);
        return Result.success(favorited);
    }

    @Operation(summary = "获取我的收藏列表")
    @GetMapping("/favorites")
    public Result<Page<FavoriteVO>> getMyFavorites(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<FavoriteVO> page = favoriteService.getMyFavorites(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取指定用户的公开收藏列表")
    @GetMapping("/user/{userId}/favorites")
    public Result<Page<FavoriteVO>> getUserPublicFavorites(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<FavoriteVO> page = favoriteService.getUserPublicFavorites(userId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "发送私信")
    @PostMapping("/message/send")
    public Result<Message> sendMessage(HttpServletRequest request, @RequestBody MessageSendDTO dto) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 发送消息给用户 {}", userId, dto.getReceiverId());
        Message message = messageService.sendMessage(userId, dto);
        return Result.success(message);
    }

    @Operation(summary = "获取聊天记录")
    @GetMapping("/messages")
    public Result<Page<MessageVO>> getConversation(HttpServletRequest request,
                                                    @RequestParam Long otherUserId,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<MessageVO> page = messageService.getConversation(userId, otherUserId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/message/{id}/read")
    public Result<Boolean> markMessageAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = messageService.markAsRead(id, userId);
        return Result.success(success);
    }

    @Operation(summary = "标记与某用户的所有未读消息为已读")
    @PutMapping("/messages/read/{otherUserId}")
    public Result<Integer> markConversationAsRead(HttpServletRequest request, @PathVariable Long otherUserId) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        int count = messageService.markConversationAsRead(userId, otherUserId);
        return Result.success(count);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/messages/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取最近的聊天对象列表")
    @GetMapping("/conversations")
    public Result<Page<MessageVO>> getRecentConversations(HttpServletRequest request,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<MessageVO> page = messageService.getRecentConversations(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "撤回消息")
    @PutMapping("/message/{id}/recall")
    public Result<Boolean> recallMessage(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 撤回消息 {}", userId, id);
        boolean success = messageService.recallMessage(userId, id);
        return Result.success(success);
    }

    @Operation(summary = "创建订单")
    @PostMapping("/order")
    public Result<TransactionOrder> createOrder(HttpServletRequest request, @RequestBody OrderCreateDTO dto) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 创建订单, 物品类型: {}, 物品ID: {}, 价格: {}", userId, dto.getItemType(), dto.getItemId(), dto.getPrice());
        TransactionOrder order = transactionOrderService.createOrder(userId, dto);
        return Result.success(order);
    }

    @Operation(summary = "获取我的购买记录")
    @GetMapping("/orders/purchases")
    public Result<Page<TransactionOrder>> getMyPurchases(HttpServletRequest request,
                                                          @RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<TransactionOrder> page = transactionOrderService.getMyPurchases(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取我的销售记录")
    @GetMapping("/orders/sales")
    public Result<Page<TransactionOrder>> getMySales(HttpServletRequest request,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<TransactionOrder> page = transactionOrderService.getMySales(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "取消订单")
    @PutMapping("/order/{id}/cancel")
    public Result<Boolean> cancelOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = transactionOrderService.cancelOrder(id, userId);
        return Result.success(success);
    }

    @Operation(summary = "发货（卖家填写快递信息）")
    @PutMapping("/order/{id}/ship")
    public Result<Boolean> shipOrder(HttpServletRequest request, @PathVariable Long id, @RequestBody OrderShipDTO dto) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 发货订单 {}, 快递单号: {}, 快递公司: {}", userId, id, dto.getTrackingNumber(), dto.getExpressCompany());
        boolean success = transactionOrderService.shipOrder(id, userId, dto);
        return Result.success(success);
    }

    @Operation(summary = "确认收货（买家确认收货）")
    @PutMapping("/order/{id}/confirm")
    public Result<Boolean> confirmReceipt(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        log.info("用户 {} 确认收货订单 {}", userId, id);
        boolean success = transactionOrderService.confirmReceipt(id, userId);
        return Result.success(success);
    }
}

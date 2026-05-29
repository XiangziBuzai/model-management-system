package com.model.management.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.MessageSendDTO;
import com.model.management.entity.Message;
import com.model.management.vo.MessageVO;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 发送消息
     * @return 创建的消息对象（包含数据库生成的ID）
     */
    Message sendMessage(Long senderId, MessageSendDTO dto);
    
    /**
     * 分页查询与某用户的聊天记录
     */
    Page<MessageVO> getConversation(Long userId, Long otherUserId, int pageNum, int pageSize);
    
    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long messageId, Long userId);
    
    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);
    
    /**
     * 获取最近的聊天对象列表
     */
    Page<MessageVO> getRecentConversations(Long userId, int pageNum, int pageSize);

    /**
     * 标记与某用户的所有未读消息为已读
     */
    int markConversationAsRead(Long userId, Long otherUserId);

    /**
     * 撤回消息
     * @param userId 当前用户ID
     * @param messageId 消息ID
     * @return 是否撤回成功
     */
    boolean recallMessage(Long userId, Long messageId);
}

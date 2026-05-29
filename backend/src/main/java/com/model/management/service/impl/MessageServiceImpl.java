package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.dto.MessageSendDTO;
import com.model.management.entity.Message;
import com.model.management.entity.User;
import com.model.management.mapper.MessageMapper;
import com.model.management.mapper.UserMapper;
import com.model.management.service.MessageService;
import com.model.management.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(Long senderId, MessageSendDTO dto) {
        if (senderId.equals(dto.getReceiverId())) {
            throw new RuntimeException("不能给自己发送消息");
        }

        // 验证接收者是否存在
        User receiver = userMapper.selectById(dto.getReceiverId());
        if (receiver == null) {
            throw new RuntimeException("接收者不存在");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setOriginalContent(dto.getContent());
        message.setIsRead(0);
        message.setIsRecalled(0);

        int result = messageMapper.insert(message);
        log.info("用户 {} 向用户 {} 发送了消息，消息ID: {}", senderId, dto.getReceiverId(), message.getId());
        return message;
    }

    @Override
    public Page<MessageVO> getConversation(Long userId, Long otherUserId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        
        // 查询两人之间的所有消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.and(sub -> sub.eq(Message::getSenderId, userId)
                                          .eq(Message::getReceiverId, otherUserId))
                          .or(sub -> sub.eq(Message::getSenderId, otherUserId)
                                        .eq(Message::getReceiverId, userId)))
               .orderByDesc(Message::getCreatedAt);
        
        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<MessageVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(messagePage.getTotal());
        
        List<MessageVO> voList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权操作此消息");
        }
        
        message.setIsRead(1);
        int result = messageMapper.updateById(message);
        return result > 0;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
               .eq(Message::getIsRead, 0);
        
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public Page<MessageVO> getRecentConversations(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getSenderId, userId)
                          .or()
                          .eq(Message::getReceiverId, userId))
               .orderByDesc(Message::getCreatedAt);
        
        List<Message> allMessages = messageMapper.selectList(wrapper);
        
        Map<Long, Message> latestMessages = new HashMap<>();
        for (Message msg : allMessages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? 
                              msg.getReceiverId() : msg.getSenderId();
            latestMessages.putIfAbsent(otherUserId, msg);
        }
        
        List<Message> recentMessages = new ArrayList<>(latestMessages.values());
        recentMessages.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        int total = recentMessages.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<MessageVO> voList = new ArrayList<>();
        if (fromIndex < total) {
            List<Message> pagedMessages = recentMessages.subList(fromIndex, toIndex);
            voList = pagedMessages.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        }
        
        for (MessageVO vo : voList) {
            Long otherUserId = vo.getSenderId().equals(userId) ? vo.getReceiverId() : vo.getSenderId();
            long unreadCount = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                    .eq(Message::getReceiverId, userId)
                    .eq(Message::getSenderId, otherUserId)
                    .eq(Message::getIsRead, 0)
            );
            vo.setUnreadCount((int) unreadCount);
        }
        
        Page<MessageVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(total);
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markConversationAsRead(Long userId, Long otherUserId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
               .eq(Message::getSenderId, otherUserId)
               .eq(Message::getIsRead, 0);
        
        List<Message> unreadMessages = messageMapper.selectList(wrapper);
        if (unreadMessages.isEmpty()) {
            return 0;
        }
        
        for (Message msg : unreadMessages) {
            msg.setIsRead(1);
        }
        
        int count = 0;
        for (Message msg : unreadMessages) {
            count += messageMapper.updateById(msg);
        }
        
        log.info("用户 {} 标记与用户 {} 的 {} 条消息为已读", userId, otherUserId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallMessage(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }

        // 验证是否是自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("无权撤回他人消息");
        }

        // 验证消息是否已撤回
        if (message.getIsRecalled() != null && message.getIsRecalled() == 1) {
            throw new RuntimeException("消息已撤回");
        }

        // 验证消息是否在两分钟内发送
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = message.getCreatedAt();
        long minutesDiff = java.time.Duration.between(createdAt, now).toMinutes();
        if (minutesDiff > 2) {
            throw new RuntimeException("消息发送超过两分钟，无法撤回");
        }

        // 保存原始内容
        if (message.getOriginalContent() == null) {
            message.setOriginalContent(message.getContent());
        }

        // 撤回消息
        message.setContent("");
        message.setIsRecalled(1);

        int result = messageMapper.updateById(message);
        log.info("用户 {} 撤回了消息 {}", userId, messageId);
        return result > 0;
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setIsRecalled(message.getIsRecalled());
        vo.setOriginalContent(message.getOriginalContent());
        vo.setCreatedAt(message.getCreatedAt());

        // 获取发送者信息
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        // 获取接收者信息
        User receiver = userMapper.selectById(message.getReceiverId());
        if (receiver != null) {
            vo.setReceiverNickname(receiver.getNickname());
            vo.setReceiverAvatar(receiver.getAvatar());
        }

        return vo;
    }
}

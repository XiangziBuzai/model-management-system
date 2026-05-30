package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.model.management.dto.AiChatMessageDTO;
import com.model.management.dto.DeepSeekRequestDTO;
import com.model.management.entity.AiChatMessage;
import com.model.management.entity.AiChatSession;
import com.model.management.mapper.AiChatMessageMapper;
import com.model.management.mapper.AiChatSessionMapper;
import com.model.management.service.AiChatService;
import com.model.management.service.DeepSeekService;
import com.model.management.vo.AiChatMessageVO;
import com.model.management.vo.AiChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final DeepSeekService deepSeekService;

    @Override
    public List<AiChatSessionVO> getSessions() {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiChatSession::getUpdatedAt);
        
        return sessionMapper.selectList(wrapper).stream()
                .map(this::convertToSessionVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiChatSessionVO createSession() {
        AiChatSession session = new AiChatSession();
        session.setUserId(1L);
        session.setTitle("新对话");
        sessionMapper.insert(session);
        
        return convertToSessionVO(session);
    }

    @Override
    @Transactional
    public boolean deleteSession(Long sessionId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getId, sessionId);
        
        return sessionMapper.delete(wrapper) > 0;
    }

    @Override
    public List<AiChatMessageVO> getMessages(Long sessionId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreatedAt);
        
        return messageMapper.selectList(wrapper).stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiChatMessageVO sendMessage(Long sessionId, AiChatMessageDTO dto) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        messageMapper.insert(userMessage);
        
        List<DeepSeekRequestDTO.Message> history = new ArrayList<>();
        List<AiChatMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByAsc(AiChatMessage::getCreatedAt)
                        .last("LIMIT 20")
        );
        
        for (AiChatMessage msg : messages) {
            if (msg.getId().equals(userMessage.getId())) {
                continue;
            }
            DeepSeekRequestDTO.Message historyMsg = new DeepSeekRequestDTO.Message();
            historyMsg.setRole(msg.getRole());
            historyMsg.setContent(msg.getContent());
            history.add(historyMsg);
        }
        
        String aiContent = deepSeekService.chat(history, dto.getContent());
        
        AiChatMessage aiMessage = new AiChatMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiContent);
        messageMapper.insert(aiMessage);
        
        session.setLastMessage(dto.getContent().length() > 50 
                ? dto.getContent().substring(0, 50) + "..." 
                : dto.getContent());
        if (session.getTitle() != null && session.getTitle().equals("新对话")) {
            session.setTitle(dto.getContent().length() > 20 
                    ? dto.getContent().substring(0, 20) + "..." 
                    : dto.getContent());
        }
        sessionMapper.updateById(session);
        
        return convertToMessageVO(aiMessage);
    }

    private AiChatSessionVO convertToSessionVO(AiChatSession session) {
        AiChatSessionVO vo = new AiChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setLastMessage(session.getLastMessage());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private AiChatMessageVO convertToMessageVO(AiChatMessage message) {
        AiChatMessageVO vo = new AiChatMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}

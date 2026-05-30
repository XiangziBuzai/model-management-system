package com.model.management.service;

import com.model.management.dto.AiChatMessageDTO;
import com.model.management.vo.AiChatMessageVO;
import com.model.management.vo.AiChatSessionVO;

import java.util.List;

public interface AiChatService {

    List<AiChatSessionVO> getSessions();

    AiChatSessionVO createSession();

    boolean deleteSession(Long sessionId);

    List<AiChatMessageVO> getMessages(Long sessionId);

    AiChatMessageVO sendMessage(Long sessionId, AiChatMessageDTO dto);
}

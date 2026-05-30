package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.dto.AiChatMessageDTO;
import com.model.management.vo.AiChatMessageVO;
import com.model.management.vo.AiChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.model.management.service.AiChatService;

import java.util.List;

@Slf4j
@Tag(name = "AI 聊天")
@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "获取对话会话列表")
    @GetMapping("/sessions")
    public Result<List<AiChatSessionVO>> getSessions() {
        try {
            return Result.success(aiChatService.getSessions());
        } catch (Exception e) {
            return Result.error(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/sessions")
    public Result<AiChatSessionVO> createSession() {
        try {
            return Result.success(aiChatService.createSession());
        } catch (Exception e) {
            return Result.error(500, "创建会话失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{id}")
    public Result<Boolean> deleteSession(@PathVariable Long id) {
        try {
            return Result.success(aiChatService.deleteSession(id));
        } catch (Exception e) {
            return Result.error(500, "删除会话失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/sessions/{id}/messages")
    public Result<List<AiChatMessageVO>> getMessages(@PathVariable Long id) {
        try {
            return Result.success(aiChatService.getMessages(id));
        } catch (Exception e) {
            return Result.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发送消息")
    @PostMapping("/sessions/{id}/messages")
    public Result<AiChatMessageVO> sendMessage(@PathVariable Long id,
                                                 @Valid @RequestBody AiChatMessageDTO dto) {
        try {
            return Result.success(aiChatService.sendMessage(id, dto));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }
}

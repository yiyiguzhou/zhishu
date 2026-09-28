package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.AssistantChatRequest;
import com.zhishu.service.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/** 大模型学习助手答疑接口。 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    /** 流式问答（Web，SSE 打字机）。 */
    @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AssistantChatRequest request) {
        return assistantService.chatStream(request);
    }

    /** 整体问答（小程序）。 */
    @PostMapping("/chat/sync")
    public ApiResponse<Map<String, String>> chatSync(@Valid @RequestBody AssistantChatRequest request) {
        String content = assistantService.chatOnce(request);
        return ApiResponse.ok(Map.of("content", content));
    }
}

package com.zhishu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** 学习助手提问：携带多轮消息历史（role: system/user/assistant，content: 文本）。 */
@Data
public class AssistantChatRequest {

    @Valid
    @NotEmpty(message = "消息不能为空")
    private List<ChatMessage> messages;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}

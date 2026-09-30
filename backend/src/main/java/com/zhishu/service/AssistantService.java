package com.zhishu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhishu.common.BusinessException;
import com.zhishu.common.UserContext;
import com.zhishu.config.AssistantProperties;
import com.zhishu.dto.AssistantChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型学习助手：转发多轮对话到火山方舟（OpenAI 兼容 chat completions）。
 * Web 走 SSE 流式，小程序走整体返回；仅登录用户可用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final String SYSTEM_PROMPT = """
            你是"纸书"大模型学习平台的学习助手，职责是解答用户在大模型学习过程中的疑问，
            内容围绕平台课程（RAG、Agent/Harness、MCP、提示词工程、AI 编程等）展开。
            回答要求：准确、简洁、有条理；不确定时明确说明，并建议用户观看平台上的对应课程。
            """;

    private final AssistantProperties properties;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;

    /** 流式问答（Web）：逐增量推送文本。 */
    public SseEmitter chatStream(AssistantChatRequest request) {
        UserContext.require();
        SseEmitter emitter = new SseEmitter(0L);
        Map<String, Object> payload = payload(request, true);

        taskExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            log.info("调用模型（流式）model={} 消息{}条", properties.getModel(), payload.get("messages") == null ? 0 : ((List<?>) payload.get("messages")).size());
            try {
                org.springframework.web.client.RestClient.create(properties.getBaseUrl())
                        .post()
                        .uri("/chat/completions")
                        .header("Authorization", "Bearer " + properties.getApiKey())
                        .body(payload)
                        .exchange((req, resp) -> {
                            if (resp.getStatusCode().isError()) {
                                throw new BusinessException(500, "模型服务调用失败：" + resp.getStatusCode());
                            }
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(resp.getBody(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (!line.startsWith("data:")) {
                                        continue;
                                    }
                                    String data = line.substring(5).trim();
                                    if ("[DONE]".equals(data)) {
                                        break;
                                    }
                                    String delta = extractDelta(data);
                                    if (!delta.isEmpty()) {
                                        emitter.send(SseEmitter.event().data(delta,
                                                org.springframework.http.MediaType.TEXT_PLAIN));
                                    }
                                }
                            }
                            return null;
                        });
                emitter.complete();
                log.info("模型流式返回完成 耗时{}ms", System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("模型流式调用异常：{}", e.getMessage());
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** 整体问答（小程序）：返回完整回复文本。 */
    public String chatOnce(AssistantChatRequest request) {
        UserContext.require();
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(500, "学习助手密钥未配置");
        }
        log.info("调用模型（非流式）model={}", properties.getModel());
        JsonNode resp = org.springframework.web.client.RestClient.create(properties.getBaseUrl())
                .post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(payload(request, false))
                .retrieve()
                .body(JsonNode.class);
        if (resp == null || !resp.path("choices").isArray()) {
            throw new BusinessException(500, "模型返回格式异常");
        }
        return resp.path("choices").get(0).path("message").path("content").asText();
    }

    private String extractDelta(String data) throws java.io.IOException {
        JsonNode node = objectMapper.readTree(data);
        return node.path("choices").get(0).path("delta").path("content").asText("");
    }

    private Map<String, Object> payload(AssistantChatRequest request, boolean stream) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        for (AssistantChatRequest.ChatMessage m : request.getMessages()) {
            if (m.getContent() != null && !m.getContent().isBlank()) {
                Map<String, String> msg = new LinkedHashMap<>();
                msg.put("role", "user".equals(m.getRole()) || "assistant".equals(m.getRole()) ? m.getRole() : "user");
                msg.put("content", m.getContent());
                messages.add(msg);
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);
        payload.put("stream", stream);
        return payload;
    }
}

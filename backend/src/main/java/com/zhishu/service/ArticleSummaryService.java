package com.zhishu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhishu.common.BusinessException;
import com.zhishu.config.AssistantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 热点文章总结：调火山方舟（OpenAI 兼容）把原文整理成中文 Markdown 总结。
 * 复用 AssistantProperties（apiKey/baseUrl/model），但不复用 AssistantService.chatOnce
 * —— 它绑定登录校验与学习助手人设，这里用独立的「内容编辑」Prompt、无需登录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSummaryService {

    private static final String SUMMARY_PROMPT = """
            你是「纸书」大模型学习平台的内容编辑。把用户提供的原文整理成一篇准确、简洁的中文 Markdown 总结。
            要求：
            1. 不照抄原文，用自己的话提炼核心观点；
            2. 用 ## 二级标题分节、要点列表呈现关键信息；
            3. 结尾用一句话给出结论或建议；
            4. 只输出 Markdown 正文，不要任何额外解释或「以下是总结」之类的开场白。
            """;

    private final AssistantProperties properties;

    public String summarize(String rawText) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(500, "AI 密钥未配置");
        }
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SUMMARY_PROMPT));
        messages.add(Map.of("role", "user", "content", rawText));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);
        payload.put("stream", false);

        log.info("调用模型生成文章总结 model={} 原文长度={}", properties.getModel(), rawText.length());
        JsonNode resp = RestClient.create(properties.getBaseUrl())
                .post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
        if (resp == null || !resp.path("choices").isArray()) {
            throw new BusinessException(500, "模型返回格式异常");
        }
        return resp.path("choices").get(0).path("message").path("content").asText();
    }
}
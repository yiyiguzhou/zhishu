package com.zhishu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 大模型学习助手配置：火山方舟 OpenAI 兼容接口。 */
@Data
@Component
@ConfigurationProperties(prefix = "zhishu.assistant")
public class AssistantProperties {
    /** Ark API Key（经 Nacos nanny-monitor-api-key.properties 注入） */
    private String apiKey;
    /** OpenAI 兼容基础地址，默认火山方舟北京 */
    private String baseUrl;
    /** 模型 ID（doubao-seed-2.1-lite 版本） */
    private String model;
}

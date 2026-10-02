package com.zhishu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网页正文抓取配置：域名白名单（防 SSRF）+ 是否启用 JS 渲染。
 * 线上 ECS 内存小，无头浏览器默认关闭（render-enabled=false）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "zhishu.crawler")
public class CrawlerProperties {
    /** 允许抓取的域名白名单（仅 host，不带 scheme/端口）。 */
    private List<String> allowedDomains = new ArrayList<>();
    /** 是否允许匹配白名单域名的子域名（默认仅精确匹配）。 */
    private boolean allowSubdomains = false;
    /** 是否启用无头浏览器 JS 渲染（需系统装有 Chrome）。 */
    private boolean renderEnabled = false;
    /** Chrome 可执行文件路径（默认依赖 PATH，找不到则抓取时降级静态）。 */
    private String chromePath = "google-chrome";
    /** 连接/读取超时（毫秒）。 */
    private int timeoutMs = 15000;
    /** 响应体上限字节（默认 2MB）。 */
    private int maxBytes = 2 * 1024 * 1024;
    /** 重定向最大跳数。 */
    private int maxRedirects = 5;
}
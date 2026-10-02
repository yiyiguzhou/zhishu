package com.zhishu.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** AI 热点文章录入请求：rawText 为原文正文，后端调用大模型生成 Markdown 总结。 */
@Data
public class ArticleIngestRequest {
    private String title;
    private Long bloggerId;
    private String cover;
    private String sourceUrl;
    private String sourceTitle;
    private String authorName;
    private String categoryKey;
    private Integer hotScore;
    private LocalDateTime publishedAt;
    /** 原文正文（人工粘贴），用于 AI 总结；MVP 不做自动抓取。 */
    private String rawText;
}
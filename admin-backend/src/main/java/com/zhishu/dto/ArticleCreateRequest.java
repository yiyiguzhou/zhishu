package com.zhishu.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 文章创建请求：三种方式之一。manual: 手动 markdown；rawText: AI 总结；fetch: 抓 URL。 */
@Data
public class ArticleCreateRequest {
    private String title;
    private Long bloggerId;
    private String cover;

    // AI 方式（贴原文）
    private String rawText;
    // AI 方式（抓 URL）
    private String sourceUrl;
    private Boolean fetch;

    // 手动方式
    private String contentMd;

    private String sourceTitle;
    private String authorName;
    private String categoryKey;
    private Integer hotScore;
    private LocalDateTime publishedAt;
    /** 创建方式：manual | raw | fetch */
    private String mode;
}
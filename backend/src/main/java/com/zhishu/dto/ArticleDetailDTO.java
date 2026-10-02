package com.zhishu.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailDTO {
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private String contentMd;
    private String contentHtml;
    private String sourceUrl;
    private String sourceTitle;
    private String authorName;
    private String categoryKey;
    private Integer hotScore;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
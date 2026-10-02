package com.zhishu.dto;

import lombok.Data;

@Data
public class ArticleDTO {
    private Long id;
    private String title;
    private String cover;
    private String categoryKey;
    private Integer hotScore;
    private String authorName;
    private String summary;
    private java.time.LocalDateTime publishedAt;
    private java.time.LocalDateTime createdAt;
}
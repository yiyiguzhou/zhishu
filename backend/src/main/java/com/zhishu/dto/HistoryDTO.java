package com.zhishu.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 浏览历史列表项。 */
@Data
public class HistoryDTO {
    private Long id;
    private String targetType;
    private Long targetId;
    private Integer watchedProgress;
    private LocalDateTime lastWatchedAt;
    private String title;
    private String cover;
}
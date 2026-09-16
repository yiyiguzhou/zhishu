package com.zhishu.dto;

import lombok.Data;

/** 视频列表项。 */
@Data
public class VideoDTO {
    private Long id;
    private String title;
    private String cover;
    private Integer duration;
    private String categoryKey;
    private Integer hotScore;
    private String authorName;   // 博主名，播放详情页左上方
    private java.time.LocalDateTime createdAt;
}
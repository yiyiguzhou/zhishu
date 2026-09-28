package com.zhishu.dto;

import lombok.Data;

import java.util.List;

/** 播放详情页数据：作者信息 + 收藏状态 + 播放地址。 */
@Data
public class VideoDetailDTO {
    private Long id;
    private String title;
    private String cover;
    private Integer duration;
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private Long bloggerId;
    private String authorName;       // 作者名称（左上方）
    private String authorAvatar;
    private boolean favorited;       // 当前用户是否已收藏
    private String playUrl;          // 经 StreamSource 解析后的播放地址
    private Long favoriteCount;
}
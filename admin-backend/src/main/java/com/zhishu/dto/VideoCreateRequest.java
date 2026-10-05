package com.zhishu.dto;

import lombok.Data;

import java.util.List;

/** 视频创建/导入请求：单个视频或批量（mediaKey 已由上传接口填充）。 */
@Data
public class VideoCreateRequest {
    private String title;
    private Long bloggerId;
    private Long categoryId;
    private String cover;
    private String mediaKey;
    private Integer duration;
    private Integer hotScore;
    private String sourceType;
    private List<Long> tagIds;
}
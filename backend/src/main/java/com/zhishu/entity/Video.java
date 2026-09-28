package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video")
public class Video {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long bloggerId;
    private Long categoryId;
    private String cover;
    private String mediaKey;
    private Integer duration;
    private Integer hotScore;
    private String sourceType;
    private String playUrl;
    private LocalDateTime createdAt;
}
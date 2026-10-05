package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("video_tag")
public class VideoTag {
    private Long videoId;
    private Long tagId;
}
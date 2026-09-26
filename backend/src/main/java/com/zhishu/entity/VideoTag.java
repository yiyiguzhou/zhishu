package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** video_tag 关联表（联合主键，只读查询用，无独立 id）。 */
@Data
@TableName("video_tag")
public class VideoTag {
    private Long videoId;
    private Long tagId;
}

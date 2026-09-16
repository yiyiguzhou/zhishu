package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("history")
public class History {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType; // video | article
    private Long targetId;
    private Integer watchedProgress;
    private LocalDateTime lastWatchedAt;
}
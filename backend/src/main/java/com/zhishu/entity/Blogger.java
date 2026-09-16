package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blogger")
public class Blogger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private String introduction;
}
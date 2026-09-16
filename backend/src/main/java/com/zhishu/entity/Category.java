package com.zhishu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;

    /** 列名 cat_key，避免 H2/MySQL 保留字。 */
    @TableField("cat_key")
    private String catKey;

    /** 列名 cat_type；取值 video_tech | blogger。 */
    @TableField("cat_type")
    private String catType;
}
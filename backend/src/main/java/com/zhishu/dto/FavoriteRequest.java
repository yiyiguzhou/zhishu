package com.zhishu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotBlank(message = "收藏类型不能为空")
    @Pattern(regexp = "video|article", message = "收藏类型仅支持 video/article")
    private String targetType;

    @NotNull(message = "收藏对象不能为空")
    private Long targetId;
}
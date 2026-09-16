package com.zhishu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank(message = "wx code 不能为空")
    private String code;
    private String nickname;
    private String avatar;
}
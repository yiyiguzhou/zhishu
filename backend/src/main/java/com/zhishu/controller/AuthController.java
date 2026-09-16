package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.LoginRequest;
import com.zhishu.dto.LoginResponse;
import com.zhishu.dto.RegisterRequest;
import com.zhishu.dto.WechatLoginRequest;
import com.zhishu.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /** 发送验证码（骨架为占位码，见 MockSmsService）。 */
    @PostMapping("/sms-code")
    public ApiResponse<Map<String, String>> sendSms(@RequestParam @NotBlank String phone) {
        String code = authService.sendSmsCode(phone);
        // 开发期把验证码随响应返回方便联调；生产实现应去掉
        return ApiResponse.ok(Map.of("phone", phone, "mockCode", code));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    /** 微信快速登录。 */
    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest req) {
        return ApiResponse.ok(authService.wechatLogin(req));
    }
}
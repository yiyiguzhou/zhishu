package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.AdminLoginRequest;
import com.zhishu.dto.AdminLoginResponse;
import com.zhishu.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService authService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody AdminLoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }
}
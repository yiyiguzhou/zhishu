package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.common.BusinessException;
import com.zhishu.dto.AdminLoginRequest;
import com.zhishu.dto.AdminLoginResponse;
import com.zhishu.entity.AdminUser;
import com.zhishu.mapper.AdminUserMapper;
import com.zhishu.security.AdminJwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminJwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminLoginResponse login(AdminLoginRequest req) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        return new AdminLoginResponse(jwtUtil.generateToken(user.getId()), user.getUsername());
    }
}
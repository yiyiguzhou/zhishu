package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.common.BusinessException;
import com.zhishu.common.JwtUtil;
import com.zhishu.dto.LoginRequest;
import com.zhishu.dto.LoginResponse;
import com.zhishu.dto.RegisterRequest;
import com.zhishu.dto.UserDTO;
import com.zhishu.dto.WechatLoginRequest;
import com.zhishu.entity.User;
import com.zhishu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;
    private final WechatService wechatService;

    public String sendSmsCode(String phone) {
        return smsService.sendCode(phone);
    }

    /** 手机号注册 + 登录，返回 token。 */
    public LoginResponse register(RegisterRequest req) {
        smsService.verify(req.getPhone(), req.getCode());

        Long exist = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (exist != null && exist > 0) {
            throw new BusinessException(400, "该手机号已注册");
        }

        User user = new User();
        user.setPhone(req.getPhone());
        user.setNickname(StringUtils.hasText(req.getNickname())
                ? req.getNickname()
                : "用户" + req.getPhone().substring(Math.max(0, req.getPhone().length() - 4)));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        log.info("新用户注册 id={} 手机尾号{}", user.getId(), tail(req.getPhone()));
        return buildResponse(user);
    }

    /** 手机号 + 验证码登录（未注册则自动注册）。 */
    public LoginResponse login(LoginRequest req) {
        smsService.verify(req.getPhone(), req.getCode());

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (user == null) {
            user = new User();
            user.setPhone(req.getPhone());
            user.setNickname("用户" + tail(req.getPhone()));
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("登录时自动注册 id={} 手机尾号{}", user.getId(), tail(req.getPhone()));
        } else {
            log.info("用户登录 id={}", user.getId());
        }
        return buildResponse(user);
    }

    /** 微信快速登录：code 换 openid，未绑定则创建用户。 */
    public LoginResponse wechatLogin(WechatLoginRequest req) {
        String openid = wechatService.openid(req.getCode());
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWechatOpenid, openid));
        if (user == null) {
            user = new User();
            user.setWechatOpenid(openid);
            user.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname() : "微信用户");
            user.setAvatar(req.getAvatar());
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("微信登录新用户 id={}", user.getId());
        } else {
            log.info("微信登录用户 id={}", user.getId());
        }
        return buildResponse(user);
    }

    private static String tail(String phone) {
        return phone.substring(Math.max(0, phone.length() - 4));
    }

    public LoginResponse buildResponse(User user) {
        return new LoginResponse(jwtUtil.generateToken(user.getId()), UserDTO.of(user));
    }
}
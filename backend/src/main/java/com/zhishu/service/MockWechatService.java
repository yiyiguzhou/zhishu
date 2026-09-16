package com.zhishu.service;

import com.zhishu.config.WechatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 骨架占位实现：未配置 AppID/Secret 时，用 code 生成占位 openid，保证本地可跑通。
 * 接真实微信时替换为调用 https://api.weixin.qq.com/sns/jscode2session。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockWechatService implements WechatService {

    private final WechatProperties props;

    @Override
    public String openid(String code) {
        if (props.getAppId() == null || props.getAppId().isBlank()) {
            log.warn("[MockWechat] 未配置 AppID，使用占位 openid（code={}）", code);
            return "mock_openid_" + code;
        }
        // TODO 真实实现：RestTemplate 调 code2session，用 appSecret + appId 换取 openid
        return "mock_openid_" + code;
    }
}
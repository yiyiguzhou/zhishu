package com.zhishu.service;

import com.zhishu.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 骨架占位实现：不真正发短信，固定验证码 123456（开发可直接用）。
 * 后续替换为真实短信服务，删除或禁用本类。
 */
@Slf4j
@Service
public class MockSmsService implements SmsService {

    public static final String MOCK_CODE = "123456";
    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public String sendCode(String phone) {
        store.put(phone, MOCK_CODE);
        log.info("[MockSms] 发送验证码到 {} => {}（占位）", phone, MOCK_CODE);
        return MOCK_CODE;
    }

    @Override
    public void verify(String phone, String code) {
        String expected = store.getOrDefault(phone, MOCK_CODE);
        if (!MOCK_CODE.equals(code) && !expected.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }
    }
}
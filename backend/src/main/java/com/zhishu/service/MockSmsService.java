package com.zhishu.service;

import com.zhishu.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 占位短信实现：不真正发短信，固定验证码 123456。
 * 验证码存 Redis（5 分钟过期），多实例共享：任一实例发出的码，
 * 另一实例都能校验；未发码或过期后校验失败。
 * 后续替换为真实短信服务时沿用相同的 Redis key 规则即可。
 */
@Slf4j
@Service
public class MockSmsService implements SmsService {

    public static final String MOCK_CODE = "123456";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public MockSmsService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(String phone) {
        return "sms:code:" + phone;
    }

    @Override
    public String sendCode(String phone) {
        redis.opsForValue().set(key(phone), MOCK_CODE, CODE_TTL);
        log.info("[MockSms] 发送验证码到 {} => {}（占位）", phone, MOCK_CODE);
        return MOCK_CODE;
    }

    @Override
    public void verify(String phone, String code) {
        String expected = redis.opsForValue().get(key(phone));
        if (expected == null || !expected.equals(code)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
    }
}

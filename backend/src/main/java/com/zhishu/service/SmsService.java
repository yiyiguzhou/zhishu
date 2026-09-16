package com.zhishu.service;

/**
 * 短信验证码服务抽象。骨架阶段用 MockSmsService 返回固定码占位；
 * 接入真实短信（阿里云/腾讯云 SMS）时新增实现并替换注入即可，无需改业务代码。
 */
public interface SmsService {

    /** 发送验证码，返回用于校验的 code。 */
    String sendCode(String phone);

    /** 校验验证码，失败抛 BusinessException。 */
    void verify(String phone, String code);
}
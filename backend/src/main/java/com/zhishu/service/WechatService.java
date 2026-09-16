package com.zhishu.service;

/**
 * 微信 code2session 抽象。骨架阶段用 MockWechatService 返回占位 openid，
 * 配置真实 AppID/Secret 后可替换为调用微信接口取 openid。
 */
public interface WechatService {

    /** 用 wx.login 的 code 换取 openid。 */
    String openid(String code);
}
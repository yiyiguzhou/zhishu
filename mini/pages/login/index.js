const api = require("../../utils/api");
const request = require("../../utils/request");

Page({
  data: {
    phone: "",
    code: "",
    loading: false,
    hint: ""
  },
  onPhone(e) { this.setData({ phone: e.detail.value }); },
  onCode(e) { this.setData({ code: e.detail.value }); },
  async sendCode() {
    if (!this.data.phone) { wx.showToast({ title: "请先输入手机号", icon: "none" }); return; }
    try {
      await request.get("/api/auth/sms-code", { phone: this.data.phone });
      // 开发期占位码：后端返回 mockCode（123456）
      wx.showToast({ title: "验证码已发送", icon: "none" });
      this.setData({ hint: "开发期固定验证码：123456" });
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    }
  },
  async phoneLogin() {
    if (!this.data.phone || !this.data.code) {
      wx.showToast({ title: "请输入手机号和验证码", icon: "none" });
      return;
    }
    this.setData({ loading: true });
    try {
      const res = await request.post("/api/auth/login", {
        phone: this.data.phone,
        code: this.data.code
      });
      wx.setStorageSync("zhishu_token", res.token);
      wx.setStorageSync("zhishu_user", res.user);
      wx.showToast({ title: "登录成功", icon: "success" });
      setTimeout(() => wx.switchTab({ url: "/pages/user/index" }), 600);
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
  async wechatLogin() {
    this.setData({ loading: true });
    try {
      await api.quickLogin();
      wx.showToast({ title: "微信登录成功", icon: "success" });
      setTimeout(() => wx.switchTab({ url: "/pages/user/index" }), 600);
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  }
});
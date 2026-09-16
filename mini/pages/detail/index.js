const app = getApp();
const api = require("../../utils/api");
const request = require("../../utils/request");

function fmtDuration(sec) {
  if (!sec) return "";
  return Math.floor(sec / 60) + ":" + String(sec % 60).padStart(2, "0");
}

Page({
  data: { detail: null, playerUrl: "" },
  onLoad(query) {
    this.id = query.id;
    this.load();
  },
  async load() {
    try {
      let detail = await api.videoDetail(this.id);
      // local 模式返回相对路径，补全后端地址
      let playerUrl = detail.playUrl || "";
      if (playerUrl.startsWith("/")) {
        playerUrl = app.globalData.baseUrl + playerUrl;
      }
      detail.durationText = fmtDuration(detail.duration);
      this.setData({ detail, playerUrl });
      if (api.getUser()) {
        api.addHistory(this.id).catch(() => {});
      }
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    }
  },
  async toggleFavorite() {
    const d = this.data.detail;
    if (!api.getUser()) {
      wx.navigateTo({ url: "/pages/login/index" });
      return;
    }
    try {
      if (d.favorited) {
        await request.delete("/api/user/favorites/video/" + d.id);
      } else {
        await api.addFavorite(d.id);
      }
      this.setData({ detail: { ...d, favorited: !d.favorited } });
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    }
  }
});
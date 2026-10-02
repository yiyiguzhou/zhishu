const api = require("../../utils/api");

Page({
  data: { detail: null },
  onLoad(query) {
    this.id = query.id;
    this.load();
  },
  async load() {
    try {
      const detail = await api.articleDetail(this.id);
      if (detail.publishedAt) {
        detail.publishedAt = String(detail.publishedAt).slice(0, 10);
      }
      this.setData({ detail });
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    }
  }
});
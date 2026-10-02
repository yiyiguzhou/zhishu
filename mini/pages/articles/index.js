const api = require("../../utils/api");

Page({
  data: { list: [], loading: false },
  onLoad() {
    this.load();
  },
  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh());
  },
  async load() {
    this.setData({ loading: true });
    try {
      this.setData({ list: await api.articles() });
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
  goDetail(e) {
    wx.navigateTo({ url: "/pages/article-detail/index?id=" + e.currentTarget.dataset.id });
  }
});
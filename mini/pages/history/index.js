const api = require("../../utils/api");

Page({
  data: { list: [], loading: true },
  onShow() {
    if (!api.getUser()) {
      wx.navigateTo({ url: "/pages/login/index" });
      return;
    }
    api.history().then((list) => {
      this.setData({ list: list.map((it) => ({ ...it, timeText: new Date(it.lastWatchedAt).toLocaleString() })), loading: false });
    }).catch((e) => {
      this.setData({ loading: false });
      wx.showToast({ title: e.message, icon: "none" });
    });
  },
  goDetail(e) {
    const item = e.currentTarget.dataset.item;
    if (item.targetType === "video") {
      wx.navigateTo({ url: "/pages/detail/index?id=" + item.targetId });
    }
  }
});
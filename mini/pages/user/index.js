const api = require("../../utils/api");

Page({
  data: { user: null, authed: true },
  onShow() {
    const user = api.getUser();
    this.setData({ user, authed: !!user });
    if (user) {
      api.profile().then((u) => {
        wx.setStorageSync("zhishu_user", u);
        this.setData({ user: u });
      }).catch(() => {});
    }
  },
  goLogin() {
    wx.navigateTo({ url: "/pages/login/index" });
  },
  goHistory() {
    wx.navigateTo({ url: "/pages/history/index" });
  },
  goFavorites() {
    wx.navigateTo({ url: "/pages/favorites/index" });
  },
  logout() {
    api.logout();
    this.setData({ user: null, authed: false });
  }
});
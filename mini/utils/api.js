const request = require("./request");

/** 微信快速登录：调 wx.login 拿 code 换后端 token，并持久化。 */
function quickLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: ({ code }) => {
        request
          .post("/api/auth/wechat-login", { code })
          .then((res) => {
            wx.setStorageSync("zhishu_token", res.token);
            wx.setStorageSync("zhishu_user", res.user);
            resolve(res);
          })
          .catch(reject);
      },
      fail: reject
    });
  });
}

function getUser() {
  return wx.getStorageSync("zhishu_user") || null;
}
function logout() {
  wx.removeStorageSync("zhishu_token");
  wx.removeStorageSync("zhishu_user");
}

module.exports = {
  hot: () => request.get("/api/hot", { limit: 8 }),
  categories: (type) => request.get("/api/categories", { type }),
  videosByCategory: (key) => request.get("/api/categories/" + key + "/videos"),
  bloggers: () => request.get("/api/bloggers"),
  videosByBlogger: (id) => request.get("/api/bloggers/" + id + "/videos"),
  videoDetail: (id) => request.get("/api/videos/" + id),
  profile: () => request.get("/api/user/profile"),
  favorites: () => request.get("/api/user/favorites"),
  addFavorite: (targetId) => request.post("/api/user/favorites", { targetType: "video", targetId }),
  history: () => request.get("/api/user/history"),
  addHistory: (targetId) => request.post("/api/user/history", { targetType: "video", targetId, watchedProgress: 0 }),
  quickLogin,
  getUser,
  logout
};
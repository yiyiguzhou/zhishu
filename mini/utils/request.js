const app = getApp();

/**
 * Promise 化的 wx.request：自动附带 token、解包 ApiResponse。
 * 抛错携带后端 message。
 */
function request(path, { method = "GET", data = {}, header = {} } = {}) {
  const token = wx.getStorageSync("zhishu_token");
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + path,
      method,
      data,
      header: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: "Bearer " + token } : {}),
        ...header
      },
      success(res) {
        const body = res.data;
        if (res.statusCode >= 200 && res.statusCode < 300 && body && body.code === 0) {
          resolve(body.data);
        } else {
          const msg = (body && body.message) || "请求失败";
          reject(new Error(msg));
        }
      },
      fail() {
        reject(new Error("网络异常，请确认后端服务已启动"));
      }
    });
  });
}

request.get = (path, data) => request(path, { method: "GET", data });
request.post = (path, data) => request(path, { method: "POST", data });
request.delete = (path, data) => request(path, { method: "DELETE", data });

module.exports = request;
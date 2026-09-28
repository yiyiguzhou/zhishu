// true = 线上（部署前先把 your-domain.com 替换为已备案域名，并在小程序后台
//         配置 request/downloadFile 合法域名）
// false = 本地开发（后端局域网地址；开发者工具和真机需在同一 Wi-Fi，
//         并勾选“不校验合法域名、TLS、HTTPS 证书”）
const USE_PROD = false;

App({
  globalData: {
    baseUrl: USE_PROD
      ? "https://your-domain.com"
      : "http://192.168.1.175:8080"
  }
});

import axios from "axios";

const client = axios.create({ baseURL: "" });

// 请求拦截：附加登录 token
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("zhishu_token");
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截：统一 ApiResponse 解包
client.interceptors.response.use(
  (res) => res.data?.data ?? res.data,
  (err) => {
    const msg = err?.response?.data?.message || err?.message || "请求失败";
    return Promise.reject(new Error(msg));
  }
);

export default client;
import axios from "axios";

const client = axios.create({
  baseURL: "",
  timeout: 30000
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem("admin_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (res) => {
    const body = res.data;
    if (body && typeof body.code === "number" && body.code !== 0) {
      return Promise.reject(new Error(body.message || "请求失败"));
    }
    return body.data;
  },
  (err) => {
    const msg = err?.response?.data?.message || err.message || "网络错误";
    if (err?.response?.status === 401) {
      localStorage.removeItem("admin_token");
      localStorage.removeItem("admin_username");
      window.location.href = "/login";
    }
    return Promise.reject(new Error(msg));
  }
);

export default client;
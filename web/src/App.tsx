import { useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import MainLayout from "./layouts/MainLayout";
import Home from "./pages/Home";
import Detail from "./pages/Detail";
import Articles from "./pages/Articles";
import ArticleDetail from "./pages/ArticleDetail";
import Login from "./pages/Login";
import UserCenter from "./pages/user/UserCenter";
import UserHistory from "./pages/user/UserHistory";
import UserFavorites from "./pages/user/UserFavorites";
import { api } from "./api";
import { useAuth } from "./store/auth";

export default function App() {
  const token = useAuth((s) => s.token);
  const setUser = useAuth((s) => s.setUser);
  const logout = useAuth((s) => s.logout);

  // 页面打开时恢复登录态：有 token 就拉用户信息；token 失效则清除
  useEffect(() => {
    if (!token) return;
    api
      .profile()
      .then((u) => setUser(u))
      .catch(() => logout());
  }, [token, setUser, logout]);

  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="video/:id" element={<Detail />} />
            <Route path="articles" element={<Articles />} />
            <Route path="articles/:id" element={<ArticleDetail />} />
            <Route path="login" element={<Login />} />
            <Route path="user" element={<UserCenter />} />
            <Route path="user/history" element={<UserHistory />} />
            <Route path="user/favorites" element={<UserFavorites />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}
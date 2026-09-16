import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import MainLayout from "./layouts/MainLayout";
import Home from "./pages/Home";
import Detail from "./pages/Detail";
import Login from "./pages/Login";
import UserCenter from "./pages/user/UserCenter";
import UserHistory from "./pages/user/UserHistory";
import UserFavorites from "./pages/user/UserFavorites";

export default function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="video/:id" element={<Detail />} />
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
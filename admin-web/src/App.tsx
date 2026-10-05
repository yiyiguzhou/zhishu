import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import MainLayout from "./layouts/MainLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Bloggers from "./pages/Bloggers";
import Videos from "./pages/Videos";
import Articles from "./pages/Articles";
import Tags from "./pages/Tags";
import Categories from "./pages/Categories";
import { useAuth } from "./store/auth";

export default function App() {
  const token = useAuth((s) => s.token);

  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={token ? <MainLayout /> : <Navigate to="/login" replace />}>
            <Route index element={<Dashboard />} />
            <Route path="bloggers" element={<Bloggers />} />
            <Route path="videos" element={<Videos />} />
            <Route path="articles" element={<Articles />} />
            <Route path="tags" element={<Tags />} />
            <Route path="categories" element={<Categories />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}
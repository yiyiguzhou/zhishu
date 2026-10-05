import { Outlet, useNavigate, useLocation, Link } from "react-router-dom";
import { Layout, Menu, Button, Space } from "antd";
import {
  DashboardOutlined,
  UserOutlined,
  VideoCameraOutlined,
  FileTextOutlined,
  TagsOutlined,
  AppstoreOutlined,
  LogoutOutlined
} from "@ant-design/icons";
import { useAuth } from "../store/auth";

const { Header, Sider, Content } = Layout;

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { username, logout } = useAuth();

  const menuItems = [
    { key: "/", icon: <DashboardOutlined />, label: "概览" },
    { key: "/bloggers", icon: <UserOutlined />, label: "作者" },
    { key: "/videos", icon: <VideoCameraOutlined />, label: "视频" },
    { key: "/articles", icon: <FileTextOutlined />, label: "文章" },
    { key: "/tags", icon: <TagsOutlined />, label: "标签" },
    { key: "/categories", icon: <AppstoreOutlined />, label: "分类" }
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider theme="dark" width={200}>
        <div style={{ color: "#fff", fontSize: 16, fontWeight: 600, padding: "16px 20px" }}>
          纸书 · 运营后台
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={(e) => navigate(e.key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: "#fff", display: "flex", justifyContent: "flex-end", alignItems: "center", padding: "0 24px" }}>
          <Space>
            <span>{username}</span>
            <Button
              icon={<LogoutOutlined />}
              onClick={() => {
                logout();
                navigate("/login");
              }}
            >
              退出
            </Button>
          </Space>
        </Header>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
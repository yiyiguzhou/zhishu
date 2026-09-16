import { Outlet, Link, useNavigate } from "react-router-dom";
import { Layout, Menu, Button, Space, Dropdown } from "antd";
import { UserOutlined, HomeOutlined, LogoutOutlined } from "@ant-design/icons";
import { useAuth } from "../store/auth";

const { Header, Content } = Layout;

export default function MainLayout() {
  const { token, user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Header style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <Link to="/" style={{ color: "#fff", fontSize: 18, fontWeight: 600 }}>
          纸书 · 大模型学习
        </Link>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[]}
          items={[{ key: "home", icon: <HomeOutlined />, label: <Link to="/">首页</Link> }]}
          style={{ flex: 1, minWidth: 0 }}
        />
        <Space>
          {token && user ? (
            <Dropdown
              menu={{
                items: [
                  { key: "center", label: "个人中心", onClick: () => navigate("/user") },
                  { key: "out", icon: <LogoutOutlined />, label: "退出登录", onClick: logout }
                ]
              }}
            >
              <Button type="text" style={{ color: "#fff" }} icon={<UserOutlined />}>
                {user.nickname}
              </Button>
            </Dropdown>
          ) : (
            <Button type="primary" onClick={() => navigate("/login")}>
              登录 / 注册
            </Button>
          )}
        </Space>
      </Header>
      <Content style={{ padding: "0 24px" }}>
        <div style={{ maxWidth: 1080, margin: "24px auto" }}>
          <Outlet />
        </div>
      </Content>
    </Layout>
  );
}
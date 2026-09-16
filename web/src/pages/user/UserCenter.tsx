import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { Card, Avatar, Descriptions, List, Tag, Button, Spin, Typography } from "antd";
import { HistoryOutlined, HeartOutlined, UserOutlined } from "@ant-design/icons";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { UserDTO } from "../../api/types";

const { Text } = Typography;

export default function UserCenter() {
  const navigate = useNavigate();
  const { token, setUser } = useAuth();
  const [user, setLocalUser] = useState<UserDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) return;
    (async () => {
      try {
        const u = await api.profile();
        setLocalUser(u);
        setUser(u);
      } finally {
        setLoading(false);
      }
    })();
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (loading && !user) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  const items = [
    {
      key: "history",
      icon: <HistoryOutlined />,
      title: "浏览历史",
      description: "最近观看的视频与文章",
      onClick: () => navigate("/user/history")
    },
    {
      key: "favorites",
      icon: <HeartOutlined />,
      title: "我的收藏",
      description: "收藏的视频",
      onClick: () => navigate("/user/favorites")
    }
  ];

  return (
    <Card style={{ maxWidth: 640, margin: "0 auto" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
        <Avatar size={64} src={user?.avatar || "https://placehold.co/64"} icon={<UserOutlined />} />
        <div>
          <div style={{ fontSize: 18, fontWeight: 600 }}>{user?.nickname}</div>
          <Text type="secondary">账号 ID：{user?.id}</Text>
        </div>
      </div>

      <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
        <Descriptions.Item label="手机号">
          {user?.phone || <Tag color="orange">微信登录用户</Tag>}
        </Descriptions.Item>
        <Descriptions.Item label="昵称">{user?.nickname}</Descriptions.Item>
        <Descriptions.Item label="注册时间">
          {new Date(user!.createdAt).toLocaleString()}
        </Descriptions.Item>
      </Descriptions>

      <List
        itemLayout="horizontal"
        dataSource={items}
        renderItem={(item) => (
          <List.Item onClick={item.onClick} style={{ cursor: "pointer" }}>
            <List.Item.Meta
              avatar={item.icon}
              title={item.title}
              description={item.description}
            />
            <Button type="link">进入 →</Button>
          </List.Item>
        )}
      />
    </Card>
  );
}
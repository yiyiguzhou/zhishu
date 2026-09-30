import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import {
  Card, Avatar, Button, Spin, Typography, Row, Col, Image, Space,
} from "antd";
import {
  HistoryOutlined, HeartOutlined, UserOutlined, ArrowRightOutlined,
} from "@ant-design/icons";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { UserDTO, HistoryDTO } from "../../api/types";

const { Text, Title } = Typography;

/** 小型内容卡片：封面 + 标题，点击进入对应视频详情。 */
function MiniContentCard({ item, onClick }: { item: HistoryDTO; onClick: () => void }) {
  return (
    <Card hoverable size="small" cover={
      <Image src={item.cover || "https://placehold.co/400x225"} preview={false}
        alt={item.title} fallback="https://placehold.co/400x225"
        style={{ aspectRatio: "16/9", objectFit: "cover" }} />
    } onClick={onClick}>
      <Text ellipsis style={{ fontSize: 13 }}>
        {item.title || `内容 #${item.targetId}`}
      </Text>
    </Card>
  );
}

export default function UserCenter() {
  const navigate = useNavigate();
  const { token, setUser } = useAuth();
  const [user, setLocalUser] = useState<UserDTO | null>(null);
  const [favorites, setFavorites] = useState<HistoryDTO[]>([]);
  const [history, setHistory] = useState<HistoryDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) return;
    (async () => {
      try {
        const [u, fav, hist] = await Promise.all([
          api.profile(),
          api.favorites().catch(() => [] as HistoryDTO[]),
          api.history().catch(() => [] as HistoryDTO[]),
        ]);
        setLocalUser(u);
        setUser(u);
        setFavorites(fav);
        setHistory(hist);
      } finally {
        setLoading(false);
      }
    })();
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (loading && !user) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  const goVideo = (item: HistoryDTO) =>
    item.targetType === "video" && navigate(`/video/${item.targetId}`);

  return (
    <div style={{ maxWidth: 960, margin: "0 auto" }}>
      {/* 用户信息 */}
      <Card style={{ marginBottom: 16 }}>
        <Space size={16}>
          <Avatar size={56} src={user?.avatar} icon={<UserOutlined />} />
          <div>
            <div style={{ fontSize: 18, fontWeight: 600 }}>{user?.nickname}</div>
            <Text type="secondary">
              {user?.phone || "微信登录用户"} · ID {user?.id}
            </Text>
          </div>
        </Space>
      </Card>

      {/* 我的收藏：直接展示卡片 + 进入详情列表入口 */}
      <Card
        style={{ marginBottom: 16 }}
        title={<Space><HeartOutlined />我的收藏</Space>}
        extra={
          <Button type="link" size="small" onClick={() => navigate("/user/favorites")}>
            全部（{favorites.length}）<ArrowRightOutlined />
          </Button>
        }
      >
        {favorites.length === 0 ? (
          <Text type="secondary">还没有收藏内容</Text>
        ) : (
          <Row gutter={[12, 12]}>
            {favorites.slice(0, 4).map((item) => (
              <Col key={`f-${item.targetType}-${item.targetId}`} xs={12} sm={8} md={6}>
                <MiniContentCard item={item} onClick={() => goVideo(item)} />
              </Col>
            ))}
          </Row>
        )}
      </Card>

      {/* 浏览历史：直接展示卡片 + 进入详情列表入口 */}
      <Card
        title={<Space><HistoryOutlined />浏览历史</Space>}
        extra={
          <Button type="link" size="small" onClick={() => navigate("/user/history")}>
            全部（{history.length}）<ArrowRightOutlined />
          </Button>
        }
      >
        {history.length === 0 ? (
          <Text type="secondary">还没有观看记录</Text>
        ) : (
          <Row gutter={[12, 12]}>
            {history.slice(0, 4).map((item) => (
              <Col key={`h-${item.targetType}-${item.targetId}`} xs={12} sm={8} md={6}>
                <MiniContentCard item={item} onClick={() => goVideo(item)} />
              </Col>
            ))}
          </Row>
        )}
      </Card>
    </div>
  );
}

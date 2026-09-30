import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { Row, Col, Spin, Typography, Image, Card, Empty } from "antd";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { HistoryDTO } from "../../api/types";

const { Text } = Typography;

/** 收藏卡片：封面 + 标题 + 收藏时间，点击整卡进入对应详情页。 */
function FavoriteCard({ item, onClick }: { item: HistoryDTO; onClick: () => void }) {
  return (
    <Card hoverable cover={
      <Image src={item.cover || "https://placehold.co/400x225"} preview={false}
        alt={item.title} fallback="https://placehold.co/400x225"
        style={{ aspectRatio: "16/9", objectFit: "cover" }} />
    } onClick={onClick}>
      <Card.Meta
        title={<Text ellipsis>{item.title || `内容 #${item.targetId}`}</Text>}
        description={
          <Text type="secondary" style={{ fontSize: 12 }}>
            收藏于 {new Date(item.lastWatchedAt).toLocaleDateString()}
          </Text>
        }
      />
    </Card>
  );
}

export default function UserFavorites() {
  const go = useNavigate();
  const { token } = useAuth();
  const [list, setList] = useState<HistoryDTO[] | null>(null);

  useEffect(() => {
    if (token)
      api.favorites().then((v) => setList(Array.isArray(v) ? v : [])).catch(() => setList([]));
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (!list) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  return (
    <div>
      <TitleText />
      {list.length === 0 ? (
        <Empty description="还没有收藏内容" style={{ marginTop: 80 }} />
      ) : (
        <Row gutter={[16, 16]}>
          {list.map((item) => (
            <Col key={`${item.targetType}-${item.targetId}`} xs={24} sm={12} md={8}>
              <FavoriteCard
                item={item}
                onClick={() =>
                  item.targetType === "video" && go(`/video/${item.targetId}`)
                }
              />
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
}

function TitleText() {
  return (
    <Typography.Title level={3} style={{ marginBottom: 16 }}>
      我的收藏
    </Typography.Title>
  );
}

import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { Row, Col, Spin, Typography, Image, Card, Progress } from "antd";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { HistoryDTO } from "../../api/types";

const { Text } = Typography;

/** 历史卡片：封面 + 标题 + 最近观看 + 进度，点击整卡续看进入详情。 */
function HistoryCard({ item, onClick }: { item: HistoryDTO; onClick: () => void }) {
  return (
    <Card hoverable cover={
      <Image src={item.cover || "https://placehold.co/400x225"} preview={false}
        alt={item.title} fallback="https://placehold.co/400x225"
        style={{ aspectRatio: "16/9", objectFit: "cover" }} />
    } onClick={onClick}>
      <Card.Meta
        title={<Text ellipsis>{item.title || `内容 #${item.targetId}`}</Text>}
        description={
          <div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              最近观看 {new Date(item.lastWatchedAt).toLocaleDateString()}
            </Text>
            {item.targetType === "video" && (
              <Progress
                percent={Math.min(100, item.watchedProgress ?? 0)}
                size="small"
                style={{ marginBottom: 0, marginTop: 4 }}
              />
            )}
          </div>
        }
      />
    </Card>
  );
}

export default function UserHistory() {
  const go = useNavigate();
  const { token } = useAuth();
  const [list, setList] = useState<HistoryDTO[] | null>(null);

  useEffect(() => {
    if (token)
      api.history().then((v) => setList(Array.isArray(v) ? v : [])).catch(() => setList([]));
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (!list) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  return (
    <div>
      <Typography.Title level={3} style={{ marginBottom: 16 }}>
        浏览历史
      </Typography.Title>
      {list.length === 0 ? (
        <Text type="secondary">还没有观看记录</Text>
      ) : (
        <Row gutter={[16, 16]}>
          {list.map((item) => (
            <Col key={`${item.targetType}-${item.targetId}`} xs={24} sm={12} md={8}>
              <HistoryCard
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

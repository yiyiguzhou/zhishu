import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { List, Spin, Tag, Typography, Image, Progress, Card } from "antd";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { HistoryDTO } from "../../api/types";

const { Text } = Typography;

function HistoryItem({ item, onClick }: { item: HistoryDTO; onClick: () => void }) {
  return (
    <List.Item onClick={onClick} style={{ cursor: "pointer" }}>
      <List.Item.Meta
        avatar={
          <Image width={90} height={56} style={{ objectFit: "cover", borderRadius: 4 }}
            src={item.cover || "https://placehold.co/90x56"} preview={false}
            fallback="https://placehold.co/90x56" />
        }
        title={item.title || `内容 #${item.targetId}`}
        description={
          <div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              最近观看：{new Date(item.lastWatchedAt).toLocaleString()}
            </Text>
            <div>
              <Tag color="blue">{item.targetType}</Tag>
            </div>
            {item.targetType === "video" && (
              <Progress percent={Math.min(100, item.watchedProgress ?? 0)} size="small"
                format={(p) => `进度 ${p}%`} />
            )}
          </div>
        }
      />
    </List.Item>
  );
}

export default function UserHistory() {
  const go = useNavigate();
  const { token } = useAuth();
  const [list, setList] = useState<HistoryDTO[] | null>(null);

  useEffect(() => {
    if (token) api.history().then(setList).catch(() => setList([]));
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (!list) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  return (
    <Card title="浏览历史" style={{ maxWidth: 720, margin: "0 auto" }}>
      <List
        dataSource={list}
        renderItem={(item) => (
          <HistoryItem item={item} onClick={() => item.targetType === "video" && go(`/video/${item.targetId}`)} />
        )}
      />
    </Card>
  );
}
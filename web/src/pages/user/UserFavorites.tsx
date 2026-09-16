import { useEffect, useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { List, Spin, Typography, Image, Card, Tag, Empty } from "antd";
import { HeartOutlined } from "@ant-design/icons";
import { api } from "../../api";
import { useAuth } from "../../store/auth";
import type { HistoryDTO } from "../../api/types";

const { Text } = Typography;

export default function UserFavorites() {
  const go = useNavigate();
  const { token } = useAuth();
  const [list, setList] = useState<HistoryDTO[] | null>(null);

  useEffect(() => {
    if (token) api.favorites().then(setList).catch(() => setList([]));
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (!list) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  return (
    <Card title="我的收藏" style={{ maxWidth: 720, margin: "0 auto" }}>
      {list.length === 0 ? (
        <Empty description="还没有收藏内容" />
      ) : (
        <List
          dataSource={list}
          renderItem={(item) => (
            <List.Item
              onClick={() => item.targetType === "video" && go(`/video/${item.targetId}`)}
              style={{ cursor: "pointer" }}
            >
              <List.Item.Meta
                avatar={
                  <Image
                    width={90}
                    height={56}
                    style={{ objectFit: "cover", borderRadius: 4 }}
                    src={item.cover || "https://placehold.co/90x56"}
                    preview={false}
                    fallback="https://placehold.co/90x56"
                  />
                }
                title={item.title || `内容 #${item.targetId}`}
                description={
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    收藏于 {new Date(item.lastWatchedAt).toLocaleString()}
                  </Text>
                }
              />
              <Tag color="red" icon={<HeartOutlined />}>已收藏</Tag>
            </List.Item>
          )}
        />
      )}
    </Card>
  );
}
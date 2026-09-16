import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Spin, Button, Tag, Typography, Space, Image, message, Row, Col } from "antd";
import { HeartOutlined, HeartFilled, VideoCameraOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { api } from "../api";
import { useAuth } from "../store/auth";
import { formatDuration } from "./Home";
import type { VideoDetailDTO } from "../api/types";

const { Title, Text } = Typography;

export default function Detail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  const [detail, setDetail] = useState<VideoDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const d = await api.videoDetail(Number(id));
        setDetail(d);
        if (token) {
          api.addHistory("video", Number(id), 0).catch(() => {});
        }
      } catch (e: any) {
        message.error(e.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  const toggleFavorite = async () => {
    if (!token) {
      message.info("请先登录");
      navigate("/login");
      return;
    }
    if (!detail) return;
    try {
      if (detail.favorited) {
        await api.removeFavorite("video", detail.id);
      } else {
        await api.addFavorite("video", detail.id);
      }
      setDetail({ ...detail, favorited: !detail.favorited });
      message.success(detail.favorited ? "已取消收藏" : "已收藏");
    } catch (e: any) {
      message.error(e.message);
    }
  };

  if (loading) return <Spin style={{ display: "block", margin: "60px auto" }} />;
  if (!detail) return <Text>视频不存在</Text>;

  return (
    <div>
      <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate("/")}>
        返回
      </Button>

      {/* 左上方：作者名称 + 收藏按钮 */}
      <Row align="middle" justify="space-between" style={{ margin: "8px 0" }}>
        <Space size="middle">
          <Image
            width={40}
            height={40}
            style={{ borderRadius: 20 }}
            src={detail.authorAvatar || "https://placehold.co/40"}
            fallback="https://placehold.co/40"
            preview={false}
          />
          <div>
            <div style={{ fontWeight: 600, fontSize: 15 }}>
              {detail.authorName || "未知作者"}
            </div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {detail.favoriteCount ?? 0} 人收藏
            </Text>
          </div>
        </Space>
        <Button
          type={detail.favorited ? "primary" : "default"}
          danger={detail.favorited}
          icon={detail.favorited ? <HeartFilled /> : <HeartOutlined />}
          onClick={toggleFavorite}
        >
          {detail.favorited ? "已收藏" : "收藏"}
        </Button>
      </Row>

      {/* 下面：视频播放器（常规播放功能） */}
      <Card>
        <video
          key={detail.playUrl}
          controls
          autoPlay
          preload="metadata"
          poster={detail.cover}
          style={{ width: "100%", maxHeight: 560, background: "#000", borderRadius: 8 }}
        >
          您的浏览器不支持 video 标签。
        </video>
        <div style={{ marginTop: 16 }}>
          <Title level={4} style={{ marginBottom: 4 }}>{detail.title}</Title>
          <Space wrap>
            <Tag icon={<VideoCameraOutlined />} color="blue">
              {formatDuration(detail.duration)}
            </Tag>
            {detail.categoryKey && <Tag color="geekblue">{detail.categoryKey}</Tag>}
          </Space>
        </div>
      </Card>
    </div>
  );
}
import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { Card, Spin, Button, Tag, Typography, Space, message } from "antd";
import { ArrowLeftOutlined, LinkOutlined } from "@ant-design/icons";
import { api } from "../api";
import Markdown from "../components/Markdown";
import type { ArticleDetailDTO } from "../api/types";

const { Title, Text } = Typography;

export default function ArticleDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [detail, setDetail] = useState<ArticleDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        setDetail(await api.articleDetail(Number(id)));
      } catch (e: any) {
        message.error(e.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  if (loading) return <Spin style={{ display: "block", margin: "60px auto" }} />;
  if (!detail) return <Text>文章不存在</Text>;

  return (
    <Card>
      <Button
        type="text"
        icon={<ArrowLeftOutlined />}
        onClick={() => {
          // 来自首页热点区则返回首页，否则返回文章列表页
          const fromHome = (location.state as any)?.fromHome;
          navigate(fromHome ? "/" : "/articles");
        }}
        style={{ marginBottom: 8 }}
      >
        返回
      </Button>

      <Title level={3} style={{ marginBottom: 8 }}>
        {detail.title}
      </Title>

      <Space wrap style={{ marginBottom: 16 }}>
        <Text type="secondary">{detail.authorName || "未知作者"}</Text>
        {detail.categoryKey && <Tag color="geekblue">{detail.categoryKey}</Tag>}
        {detail.publishedAt && <Text type="secondary">{detail.publishedAt.slice(0, 10)}</Text>}
        {detail.sourceUrl && (
          <a href={detail.sourceUrl} target="_blank" rel="noreferrer" style={{ color: "#1677ff" }}>
            <LinkOutlined /> 原文
          </a>
        )}
      </Space>

      {detail.summary && (
        <div
          style={{
            margin: "8px 0 16px",
            padding: "10px 16px",
            background: "#f5f7fa",
            borderLeft: "4px solid #1677ff",
            borderRadius: 4,
            color: "rgba(0,0,0,0.65)",
            lineHeight: 1.7
          }}
        >
          {detail.summary}
        </div>
      )}

      <Markdown content={detail.contentMd} />
    </Card>
  );
}
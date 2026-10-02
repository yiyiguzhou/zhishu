import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Row, Col, Spin, Empty, Typography, Space, Tag, Image } from "antd";
import { api } from "../api";
import type { ArticleDTO } from "../api/types";

const { Text } = Typography;

function ArticleCard({ article, onClick }: { article: ArticleDTO; onClick: () => void }) {
  return (
    <Card
      hoverable
      cover={
        <Image
          src={article.cover || "https://placehold.co/400x225"}
          alt={article.title}
          preview={false}
          style={{ aspectRatio: "16/9", objectFit: "cover" }}
          fallback="https://placehold.co/400x225"
        />
      }
      onClick={onClick}
      style={{ marginBottom: 8 }}
    >
      <Card.Meta
        title={<Text ellipsis>{article.title}</Text>}
        description={
          <Space>
            <Text type="secondary">{article.authorName || "未知作者"}</Text>
            {article.categoryKey && <Tag color="geekblue">{article.categoryKey}</Tag>}
          </Space>
        }
      />
    </Card>
  );
}

export default function Articles() {
  const navigate = useNavigate();
  const [articles, setArticles] = useState<ArticleDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setArticles(await api.articles(undefined, 20));
      } catch (e: any) {
        // 列表加载失败保持空态
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  return (
    <div>
      <Typography.Title level={4}>热点文章</Typography.Title>
      {articles.length ? (
        <Row gutter={[16, 16]}>
          {articles.map((a) => (
            <Col xs={24} sm={12} md={8} key={a.id}>
              <ArticleCard article={a} onClick={() => navigate(`/articles/${a.id}`)} />
            </Col>
          ))}
        </Row>
      ) : (
        <Empty description="暂无文章" style={{ padding: 24 }} />
      )}
    </div>
  );
}
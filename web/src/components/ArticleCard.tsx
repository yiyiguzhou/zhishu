import { Card, Typography, Space, Tag } from "antd";
import type { ArticleDTO } from "../api/types";

const { Text, Paragraph } = Typography;

/**
 * 文章卡片（区别于视频的 16:9 封面卡）：
 * 左侧纯色分类标记 + 右侧标题/摘要/作者，简约列表风，无大图。
 * 后续所有文章列表统一复用此样式。
 */
export default function ArticleCard({
  article,
  onClick
}: {
  article: ArticleDTO;
  onClick: () => void;
}) {
  return (
    <Card
      hoverable
      onClick={onClick}
      size="small"
      style={{ marginBottom: 8 }}
      styles={{ body: { padding: "14px 16px" } }}
    >
      <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
        {/* 左侧分类色块 */}
        <div
          style={{
            flexShrink: 0,
            width: 44,
            height: 44,
            borderRadius: 8,
            background: "#1677ff14",
            color: "#1677ff",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: 13,
            fontWeight: 600,
            textTransform: "uppercase"
          }}
        >
          {article.categoryKey || "AI"}
        </div>

        {/* 右侧内容 */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <Space style={{ width: "100%", justifyContent: "space-between" }}>
            <Text strong ellipsis style={{ flex: 1, fontSize: 15 }}>
              {article.title}
            </Text>
          </Space>
          {article.summary && (
            <Paragraph
              type="secondary"
              ellipsis={{ rows: 2 }}
              style={{ display: "block", marginTop: 4, marginBottom: 0, fontSize: 13, lineHeight: 1.5 }}
            >
              {article.summary}
            </Paragraph>
          )}
          <Space size={8} style={{ marginTop: 6 }}>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {article.authorName || "未知作者"}
            </Text>
            {article.publishedAt && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {article.publishedAt.slice(0, 10)}
              </Text>
            )}
            {article.categoryKey && <Tag color="geekblue" style={{ marginInlineEnd: 0 }}>{article.categoryKey}</Tag>}
          </Space>
        </div>
      </div>
    </Card>
  );
}
import { Card, Typography, Space, Tag } from "antd";
import type { ArticleDTO } from "../api/types";

const { Text, Paragraph } = Typography;

/**
 * 文章卡片（区别于视频的 16:9 封面卡）：
 * 左侧分类色块 + 右侧标题/摘要/作者，简约列表风，无大图。
 * 统一卡片高度：标题单行省略、摘要固定两行（无摘要也占位）、meta 固定一行。
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
      <div
        style={{
          display: "flex",
          alignItems: "flex-start",
          gap: 12,
          height: 120
        }}
      >
        {/* 左侧分类色块 */}
        <div
          style={{
            flexShrink: 0,
            width: 56,
            height: 56,
            padding: "4px",
            borderRadius: 8,
            background: "#1677ff14",
            color: "#1677ff",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            textAlign: "center",
            wordBreak: "break-all",
            fontSize: 11,
            lineHeight: 1.2,
            fontWeight: 600,
            textTransform: "uppercase"
          }}
        >
          {article.categoryKey || "AI"}
        </div>

        {/* 右侧内容 */}
        <div style={{ flex: 1, minWidth: 0, height: "100%", display: "flex", flexDirection: "column" }}>
          {/* 标题：单行省略 */}
          <Text
            strong
            ellipsis
            title={article.title}
            style={{ display: "block", fontSize: 15, lineHeight: "22px" }}
          >
            {article.title}
          </Text>

          {/* 摘要：固定两行，无摘要时占位保持卡片等高 */}
          <Paragraph
            type="secondary"
            ellipsis={{ rows: 2 }}
            style={{
              display: "-webkit-box",
              marginTop: 4,
              marginBottom: 0,
              fontSize: 13,
              lineHeight: "20px",
              minHeight: "40px",
              overflow: "hidden"
            }}
          >
            {article.summary || ""}
          </Paragraph>

          {/* meta：固定一行 */}
          <Space size={8} style={{ marginTop: "auto" }}>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {article.authorName || "未知作者"}
            </Text>
            {article.publishedAt && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {article.publishedAt.slice(0, 10)}
              </Text>
            )}
            {article.categoryKey && (
              <Tag color="geekblue" style={{ marginInlineEnd: 0 }}>
                {article.categoryKey}
              </Tag>
            )}
          </Space>
        </div>
      </div>
    </Card>
  );
}
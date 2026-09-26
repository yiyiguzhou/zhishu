-- 演示种子数据：博主 + 两类分类 + 视频（关联类别/作者/标签） + 标签 + 文章
-- media_key 存储中立：当前为 NAS 共享相对路径，将来可直接作为 OSS Object Key。

INSERT INTO blogger (id, name, avatar, introduction) VALUES
  (1, '李某某的AI课', 'https://placehold.co/200?text=AI%E8%AF%BE', '专注大模型工程化落地，热衷分享 RAG 实战'),
  (2, 'Harness实验室', 'https://placehold.co/200?text=Harness', '深入解析 Agent 与工具编排、Harness 框架'),
  (3, 'MCP研究所', 'https://placehold.co/200?text=MCP', '研究 Model Context Protocol 协议与生态'),
  (4, 'LLM入门指南', 'https://placehold.co/200?text=LLM', '零基础到大模型应用开发的学习路线');

INSERT INTO category (id, name, cat_key, cat_type) VALUES
  (1, '智能体框架', 'harness', 'video_tech'),
  (2, 'MCP协议', 'mcp', 'video_tech'),
  (3, 'RAG检索', 'rag', 'video_tech'),
  (4, '提示词工程', 'prompt', 'video_tech'),
  (5, '李某某的AI课', 'blogger_1', 'blogger'),
  (6, 'Harness实验室', 'blogger_2', 'blogger'),
  (7, 'MCP研究所', 'blogger_3', 'blogger'),
  (8, 'LLM入门指南', 'blogger_4', 'blogger');

INSERT INTO video (id, title, blogger_id, category_id, cover, media_key, duration, hot_score, source_type, play_url) VALUES
  (1, 'RAG全流程实战：从索引到生成', 1, 3, 'https://placehold.co/400x225?text=RAG', 'rag/rag-full-guide.mp4', 1520, 980, 'nas', NULL),
  (2, '用Harness编排你的第一个Agent', 2, 1, 'https://placehold.co/400x225?text=Harness', 'harness/harness-agent.mp4', 860, 810, 'nas', NULL),
  (3, 'MCP协议详解：工具即服务', 3, 2, 'https://placehold.co/400x225?text=MCP', 'mcp/mcp-tool-as-service.mp4', 1200, 760, 'nas', NULL),
  (4, '大模型RAG问答的正确姿势', 4, 3, 'https://placehold.co/400x225?text=QA', 'rag/rag-qa-mistakes.mp4', 640, 920, 'nas', NULL),
  (5, 'Harness控制台快速上手', 2, 1, 'https://placehold.co/400x225?text=Console', 'harness/harness-console.mp4', 500, 450, 'nas', NULL);

INSERT INTO tag (id, name) VALUES
  (1, 'RAG'),
  (2, '向量数据库'),
  (3, 'Embedding'),
  (4, 'Agent'),
  (5, '工具调用'),
  (6, 'MCP'),
  (7, '提示词');

INSERT INTO video_tag (video_id, tag_id) VALUES
  (1, 1), (1, 2), (1, 3),
  (2, 4), (2, 5),
  (3, 6), (3, 5),
  (4, 1), (4, 3),
  (5, 4);

INSERT INTO article (id, title, blogger_id, cover, content_url, category_key, hot_score) VALUES
  (1, '2026年做RAG，这些坑别再踩了', 1, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/rag-pits', 'rag', 880),
  (2, 'MCP能让Claude接入你的数据库吗', 3, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/mcp-db', 'mcp', 720),
  (3, 'Agent框架选型指南', 2, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/agent-select', 'harness', 690);

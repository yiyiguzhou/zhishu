-- 种子数据：博主 + 两类分类 + 真实视频（6 条，对应 MinIO bucket zhishu-video 根目录对象）+ 标签字典
-- 视频的 blogger_id/category_id 暂为空，由运营更新；media_key 即对象 key（含中文名与 # 字符，S3 兼容）。
-- 文章为补充内容种子。

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
  (9, 'AI编程', 'ai-coding', 'video_tech'),
  (10, 'AI动态', 'ai-trends', 'video_tech'),
  (11, '基础概念', 'fundamentals', 'video_tech'),
  (5, '李某某的AI课', 'blogger_1', 'blogger'),
  (6, 'Harness实验室', 'blogger_2', 'blogger'),
  (7, 'MCP研究所', 'blogger_3', 'blogger'),
  (8, 'LLM入门指南', 'blogger_4', 'blogger');

INSERT INTO video (id, title, blogger_id, category_id, cover, media_key, duration, hot_score, source_type, play_url) VALUES
  (1, 'AI最前沿的人，已经不聊模型了#aicoding #易论AI #归藏 #colaOS #李继刚', NULL, 10, NULL,
   'ai-frontier.mp4', 0, 0, 'minio', NULL),
  (2, 'Harness Engineering 到底是什么？概念、实战与争议，一次全部讲清楚', NULL, 1, NULL,
   'harness-engineering-explained.mp4', 0, 0, 'minio', NULL),
  (3, 'RAG 工作机制详解——一个高质量知识库背后的技术全流程', NULL, 3, NULL,
   'rag-workflow-deep-dive.mp4', 0, 0, 'minio', NULL),
  (4, 'Token 到底是什么？—— 揭秘大模型背后的“文字压缩术”', NULL, 11, NULL,
   'what-is-token.mp4', 0, 0, 'minio', NULL),
  (5, '如何使用第三方模型驱动 Codex（无需 OpenAI 账号）', NULL, 9, NULL,
   'drive-codex-thirdparty-models.mp4', 0, 0, 'minio', NULL),
  (6, '我的 AI 编程全流程：如何使用 AI 稳定交付一个高质量的产品', NULL, 9, NULL,
   'my-ai-coding-workflow.mp4', 0, 0, 'minio', NULL);

INSERT INTO tag (id, name) VALUES
  (1, 'RAG'),
  (2, '向量数据库'),
  (3, 'Embedding'),
  (4, 'Agent'),
  (5, '工具调用'),
  (6, 'MCP'),
  (7, '提示词');

INSERT INTO article (id, title, blogger_id, cover, content_url, category_key, hot_score) VALUES
  (1, '2026年做RAG，这些坑别再踩了', 1, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/rag-pits', 'rag', 880),
  (2, 'MCP能让Claude接入你的数据库吗', 3, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/mcp-db', 'mcp', 720),
  (3, 'Agent框架选型指南', 2, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/agent-select', 'harness', 690);

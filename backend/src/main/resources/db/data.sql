-- 种子数据：博主 + 两类分类 + 真实视频（36 条）+ 标签（7 条）+ 关联（30 条）+ 文章（6 条）
-- media_key 即对象 key：作者视频用 <作者slug>/<视频slug>.mp4（mark-tech-workshop/ 前缀），其余为 bucket 根目录平铺。
-- 封面走 OSS covers/ 前缀（anonymous download）；视频走 OSS 私有+预签名。
-- 文章为补充内容种子。

INSERT INTO blogger (id, name, avatar, introduction) VALUES
  (1, '李某某的AI课', 'https://placehold.co/200?text=AI%E8%AF%BE', '专注大模型工程化落地，热衷分享 RAG 实战'),
  (2, 'Harness实验室', 'https://placehold.co/200?text=Harness', '深入解析 Agent 与工具编排、Harness 框架'),
  (3, 'MCP研究所', 'https://placehold.co/200?text=MCP', '研究 Model Context Protocol 协议与生态'),
  (4, 'LLM入门指南', 'https://placehold.co/200?text=LLM', '零基础到大模型应用开发的学习路线'),
  (5, '马克的技术工作坊', 'https://placehold.co/200?text=mark-tech-workshop', '专注 AI 工程化与智能体实践，覆盖 MCP、Agent、RAG、AI 编程等前沿技术');

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
  (8, 'LLM入门指南', 'blogger_4', 'blogger'),
  (12, '马克的技术工作坊', 'blogger_5', 'blogger');

-- 封面：bucket covers/ 前缀（anonymous download，免登录展示）；地址用网关绝对路径。
INSERT INTO video (id, title, blogger_id, category_id, cover, media_key, duration, hot_score, source_type, play_url) VALUES
  (1, 'AI最前沿的人，已经不聊模型了#aicoding #易论AI #归藏 #colaOS #李继刚', NULL, 10,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/ai-frontier.jpg', 'ai-frontier.mp4', 0, 0, 'minio', NULL),
  (2, 'Harness Engineering 到底是什么？概念、实战与争议，一次全部讲清楚', 5, 1,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/harness-engineering-explained.jpg', 'harness-engineering-explained.mp4', 0, 0, 'minio', NULL),
  (3, 'RAG 工作机制详解——一个高质量知识库背后的技术全流程', 5, 3,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/rag-workflow-deep-dive.jpg', 'rag-workflow-deep-dive.mp4', 0, 0, 'minio', NULL),
  (4, 'Token 到底是什么？—— 揭秘大模型背后的“文字压缩术”', 5, 11,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/what-is-token.jpg', 'what-is-token.mp4', 0, 0, 'minio', NULL),
  (5, '如何使用第三方模型驱动 Codex（无需 OpenAI 账号）', 5, 9,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/drive-codex-thirdparty-models.jpg', 'drive-codex-thirdparty-models.mp4', 0, 0, 'minio', NULL),
  (6, '我的 AI 编程全流程：如何使用 AI 稳定交付一个高质量的产品', 5, 9,
   'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/my-ai-coding-workflow.jpg', 'my-ai-coding-workflow.mp4', 0, 0, 'minio', NULL),
  (7, '5分钟教会你如何本地部署DeepSeek-R1，无需联网，全程干货，没有一句废话', 5, 11, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/deploy-deepseek-r1-locally.jpg', 'mark-tech-workshop/deploy-deepseek-r1-locally.mp4', 346, 0, 'oss', NULL),
  (8, '5种使用Deepseek打造可视化内容的方法，构建创意视觉图和专业数据分析报表，增强画面冲击力，可直接用在PPT，总结报告和视频解说中，总有一款适合你！', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/deepseek-visual-content-ideas.jpg', 'mark-tech-workshop/deepseek-visual-content-ideas.mp4', 449, 0, 'oss', NULL),
  (9, '6分钟快速上手最强学习工具NotebookLM（2025）', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/notebooklm-quick-start.jpg', 'mark-tech-workshop/notebooklm-quick-start.mp4', 362, 0, 'oss', NULL),
  (10, 'A2A协议深度解析（第 1 部分：双Agent同步调用场景）', 5, 11, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/a2a-protocol-part1.jpg', 'mark-tech-workshop/a2a-protocol-part1.mp4', 2144, 0, 'oss', NULL),
  (11, 'A2A协议深度解析（第 2 部分：流式返回 + 多 Agent 场景）', 5, 11, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/a2a-protocol-part2.jpg', 'mark-tech-workshop/a2a-protocol-part2.mp4', 1037, 0, 'oss', NULL),
  (12, 'Agent Skill 从使用到原理，一次讲清', 5, 1, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/agent-skill-explained.jpg', 'mark-tech-workshop/agent-skill-explained.mp4', 1061, 0, 'oss', NULL),
  (13, 'Agent 的概念、原理与构建模式 —— 从零打造一个简化版的 Claude Code', 5, 1, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/agent-concepts-and-patterns.jpg', 'mark-tech-workshop/agent-concepts-and-patterns.mp4', 1686, 0, 'oss', NULL),
  (14, 'Claude Code 从 0 到 1 全攻略 —— MCP ⧸ SubAgent ⧸ Agent Skill ⧸ Hook ⧸ 图片 ⧸ 上下文处理⧸ 后台任务 ⧸ 权限', 5, 9, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/claude-code-complete-guide.jpg', 'mark-tech-workshop/claude-code-complete-guide.mp4', 2684, 0, 'oss', NULL),
  (15, 'Codex 从 0 到 1 全攻略 - Annotate ⧸ Fork ⧸ Archive ⧸ Plan ⧸ Plugin ⧸ Skill ⧸ Automation ⧸ Mobile', 5, 9, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/codex-complete-guide.jpg', 'mark-tech-workshop/codex-complete-guide.mp4', 3517, 0, 'oss', NULL),
  (16, 'Context Engineering：概念与技术实现深度解析', 5, 1, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/context-engineering-deep-dive.jpg', 'mark-tech-workshop/context-engineering-deep-dive.mp4', 915, 0, 'oss', NULL),
  (17, 'DeepSeek R1 真的那么强吗？客观评测 R1 与 o1 在编程、推理方面的效果', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/deepseek-r1-vs-o1-review.jpg', 'mark-tech-workshop/deepseek-r1-vs-o1-review.mp4', 1348, 0, 'oss', NULL),
  (18, 'GPT 4o 图片生成能力评测', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/gpt4o-image-generation-review.jpg', 'mark-tech-workshop/gpt4o-image-generation-review.mp4', 690, 0, 'oss', NULL),
  (19, 'Keynote 动画制作全流程，以及我对 AI 动画的一些看法', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/keynote-animation-workflow.jpg', 'mark-tech-workshop/keynote-animation-workflow.mp4', 3172, 0, 'oss', NULL),
  (20, 'MCP 与 Function Calling 到底什么关系 —— 以及为什么我认为大部分人的观点都是错误的', 5, 2, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/mcp-vs-function-calling.jpg', 'mark-tech-workshop/mcp-vs-function-calling.mp4', 1636, 0, 'oss', NULL),
  (21, 'MCP终极指南   带你深入掌握MCP（进阶篇）', 5, 2, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/mcp-guide-advanced.jpg', 'mark-tech-workshop/mcp-guide-advanced.mp4', 1640, 0, 'oss', NULL),
  (22, 'MCP终极指南 - 从原理到实战，带你深入掌握MCP（基础篇）', 5, 2, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/mcp-guide-basics.jpg', 'mark-tech-workshop/mcp-guide-basics.mp4', 1625, 0, 'oss', NULL),
  (23, 'MCP终极指南 - 番外篇：抓包分析 Cline 与模型的交互协议（内含 Agent 的实现原理）', 5, 2, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/mcp-guide-extra-capture.jpg', 'mark-tech-workshop/mcp-guide-extra-capture.mp4', 2644, 0, 'oss', NULL),
  (24, 'Manus案例深度解析 - 以及我对它的看法', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/manus-case-analysis.jpg', 'mark-tech-workshop/manus-case-analysis.mp4', 460, 0, 'oss', NULL),
  (25, 'Midjourney 2025 网页版完整教学 - 从基础到进阶', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/midjourney-2025-web-tutorial.jpg', 'mark-tech-workshop/midjourney-2025-web-tutorial.mp4', 1262, 0, 'oss', NULL),
  (26, 'OpenAI 的 RAG 范例，无需向量化', 5, 3, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/openai-rag-example.jpg', 'mark-tech-workshop/openai-rag-example.mp4', 1163, 0, 'oss', NULL),
  (27, 'Temperature & Top-p：掌控大模型的创造力开关', 5, 4, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/temperature-top-p.jpg', 'mark-tech-workshop/temperature-top-p.mp4', 1047, 0, 'oss', NULL),
  (28, 'o3-mini-high编程与推理能力评测', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/o3-mini-high-review.jpg', 'mark-tech-workshop/o3-mini-high-review.mp4', 1062, 0, 'oss', NULL),
  (29, 'o3、o4 mini high数学和推理能力评测', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/o3-o4-mini-high-math-review.jpg', 'mark-tech-workshop/o3-o4-mini-high-math-review.mp4', 1518, 0, 'oss', NULL),
  (30, '为什么越来越多的人抛弃 MCP，转向 CLI？', 5, 2, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/why-cli-over-mcp.jpg', 'mark-tech-workshop/why-cli-over-mcp.mp4', 938, 0, 'oss', NULL),
  (31, '从 LLM 到 Agent Skill，一期视频带你打通底层逻辑！', 5, 1, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/llm-to-agent-skill.jpg', 'mark-tech-workshop/llm-to-agent-skill.mp4', 1951, 0, 'oss', NULL),
  (32, '使用 Cursor 和 Claude3 7 10分钟构建产品原型图和iOS应用，无需写一行代码', 5, 9, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/cursor-claude-prototype.jpg', 'mark-tech-workshop/cursor-claude-prototype.mp4', 439, 0, 'oss', NULL),
  (33, '使用 DeepSeek R1 与 AnythingLLM 搭建本地知识库', 5, 3, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/deepseek-anythingllm-rag.jpg', 'mark-tech-workshop/deepseek-anythingllm-rag.mp4', 620, 0, 'oss', NULL),
  (34, '使用Python构建RAG系统 —— 用代码还原 RAG系统的每个细节', 5, 3, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/python-rag-from-scratch.jpg', 'mark-tech-workshop/python-rag-from-scratch.mp4', 1050, 0, 'oss', NULL),
  (35, '四大推理大模型数学与编程能力评测   Grok3、Claude3 7、DeepSeep R1、o3 mini high 到底谁的推理能力最强？', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/top4-reasoning-models-review.jpg', 'mark-tech-workshop/top4-reasoning-models-review.mp4', 1502, 0, 'oss', NULL),
  (36, '把 Gemini Flash 2 0 当 PS 使用的正确方法', 5, 10, 'https://zhishu-video-ai.oss-cn-beijing.aliyuncs.com/covers/mark-tech-workshop/gemini-flash-as-photoshop.jpg', 'mark-tech-workshop/gemini-flash-as-photoshop.mp4', 501, 0, 'oss', NULL);

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
  (3, 'Agent框架选型指南', 2, 'https://placehold.co/400x225?text=Article', 'https://example.com/articles/agent-select', 'harness', 690),
  (4, 'MCP协议深度解析：从入门到实战', 5, 'https://placehold.co/400x225?text=MCP+Deep', 'https://example.com/articles/mcp-deep-dive', 'mcp', 850),
  (5, 'Agent开发实战：用Claude Code打造你的第一个智能体', 5, 'https://placehold.co/400x225?text=Agent+Dev', 'https://example.com/articles/agent-dev', 'harness', 920),
  (6, 'RAG系统优化指南：从检索到生成的完整链路', 5, 'https://placehold.co/400x225?text=RAG+Guide', 'https://example.com/articles/rag-guide', 'rag', 780);

INSERT INTO video_tag (video_id, tag_id) VALUES
  (10, 4), (10, 5), (11, 4), (11, 5),
  (12, 4), (12, 5), (13, 4), (13, 5),
  (14, 4), (14, 5), (14, 6),
  (15, 4), (15, 5),
  (16, 4),
  (20, 6), (21, 6), (22, 6), (23, 6), (30, 6),
  (24, 4),
  (26, 1), (26, 2), (26, 3),
  (27, 7),
  (31, 4),
  (32, 4), (32, 5),
  (33, 1), (33, 2), (33, 3),
  (34, 1), (34, 2), (34, 3);
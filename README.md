# 纸书 · 大模型学习平台

大模型学习平台的 **三端骨架**（Java 后端 / React Web / 微信小程序）。
当前为项目骨架：统一技术底座与核心数据模型已就位，功能页可跑通主链路，后续逐端填充完整功能。

## 目录结构

```
zhishu/
├── backend/   Spring Boot(Java 17) + Spring Cloud Alibaba Nacos + MyBatis-Plus + MySQL(默认)/H2 + JWT + MinIO 抽象
├── web/       React 18 + TypeScript + Vite + Ant Design + Zustand
└── mini/      原生微信小程序（WXML + JS）
```

> 📖 更多文档见 [doc/ 文档中心](doc/README.md)：技术架构、业务架构、变更记录

## 核心设计

- **视频来源抽象 `StreamSource`**：后端按 `zhishu.media.mode` 在 `local`/`minio`/`oss`/`nas`
  间切换；新增存储（如别的对象存储）只需新增一个 Resolver。
- **视频元数据模型**：video 经 `category_id` 外键关联 `category`（视频类别）、
  `blogger_id` 关联 `blogger`（作者），标签走 `tag` + `video_tag` 多对多。
- **两种分类共用 category 表**：按技术(`video_tech`，harness/mcp/rag…) 或按博主(`blogger`)。
- **认证**：小程序微信快速登录(`wx.login`→openid)；Web 手机号+验证码（开发期占位码 `123456`）。

## Nacos 服务架构

后端已接入 **Nacos 2.x（服务注册发现 + 配置中心）**，开发环境 Nacos 在局域网 `192.168.1.38`（Docker 单机版，与仓库内 [deploy/nacos/docker-compose.yml](deploy/nacos/docker-compose.yml) 一致）。

- **控制台**：http://192.168.1.38:8848/nacos ，初始账号 `nacos/nacos`（尽快改密）
- **注册发现**：服务名 `zhishu-backend`，分组 `ZHISHU_GROUP`，实例带本机 IP+8080 注册；后续新增服务（如搜索/推荐）直接注册即可互相发现
- **配置中心**：group 同为 `ZHISHU_GROUP`，应用监听两个 dataId（均可选，没有也能启动）：
  - `zhishu-backend.yaml`：所有 profile 共用的动态配置，已发布示例（在线调日志级别）
  - `zhishu-backend-<profile>.yaml`：profile 专属配置
  - 在控制台改完保存，约 10 秒自动推送到应用，**不用重启**（已验证：远程改日志级别即时生效）
- **连别的 Nacos** 或本机自建：设置环境变量即可，不建议把地址写死，支持覆盖：
  `NACOS_SERVER_ADDR`（如 `127.0.0.1:8848`）、`NACOS_USERNAME`、`NACOS_PASSWORD`
- Nacos 暂时连不上不阻断后端启动（`fail-fast=false`，恢复后自动重连并重新注册）

## 视频存储网关（MinIO）

视频文件统一由独立网关机（`192.168.1.38`）上的 MinIO 管理，数据目录是该 Mac 本机磁盘上的
`~/Documents/YouTube`（**本地调试不依赖 NAS**）；zhishu 后端只是 S3 客户端，
解析出预签名播放 URL，播放器直连网关。

> MinIO 官方开源镜像 2025-10 起停发并从 Docker Hub 下架，现使用逐行兼容的社区分支
> **pgsty/minio**（server 命令、环境变量、`.minio.sys` 磁盘格式、Web 控制台完全一致）。

```
~/Documents/YouTube ─bind─▶ pgsty/minio(:9000 API / :9001 控制台)
zhishu-backend(.175, mode=minio) ─预签名URL─▶ Web/小程序播放器直连 .38:9000
```

**网关机 .38 部署（工件在 `deploy/minio/`）**
```bash
cp deploy/minio/.env.example deploy/minio/.env   # MinIO 管理员（密码 >=8 位）
docker compose -f deploy/minio/docker-compose.yml up -d
# 首次绑定 ~/Documents 时 macOS 会弹隐私授权，请在网关机屏幕上允许
# 控制台 http://192.168.1.38:9001 ，bucket：zhishu-video
```

**应用服务器 .175**：启动时注入网关凭据（不入库）：
`MINIO_ENDPOINT`（默认已指向 .38）、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`、`MINIO_BUCKET`(默认 zhishu-video)

- 对象经 S3 API/9001 控制台上传；浏览器播放要求 MP4/H.264/AAC
- 视频模式按 `zhishu.media.mode` 切换：`minio`(默认，网关) / `nas`(后端本机直挂 NAS，脚本 `scripts/`) / `local`
- 上阿里云 OSS 时 media_key 直接复用为 Object Key，仅需改 endpoint/凭据并新增 oss Resolver

## 大模型学习助手

全站答疑 Agent（仅登录用户可用）：Web 右下角悬浮按钮打开聊天窗（SSE 打字机输出）；
小程序悬浮球进入独立 chat 页（整体返回）。

- **模型**：火山方舟 OpenAI 兼容接口，默认 `deepseek-v4-flash-260425`（`ARK_MODEL` 可覆盖）
- **密钥**：配置在 Nacos（ZHISHU_GROUP）`zhishu-backend.yaml` 的 `spring.ai.openai.api-key`，
  不写入代码库；应用 `zhishu.assistant.api-key` 经该属性注入
- 接口：`POST /api/assistant/chat`（SSE 流式）、`POST /api/assistant/chat/sync`（整体返回）
- 多轮对话历史由客户端持有、随请求上送；系统提示词固化在 AssistantService

## 环境划分（本地开发 / 线上）

两套环境完全独立，**同一份代码、不同的配置来源**，互不影响：

| | 本地开发 | 线上正式 |
|---|---|---|
| 代码位置 | 本机仓库 | ECS `/opt/zhishu`（182.92.124.62，北京） |
| 配置来源 | application.yml 默认值 + `.dev-secrets`（gitignore） | ECS 上的 `/opt/zhishu/deploy/.env.prod`（gitignore） |
| MySQL / Nacos / Redis | 局域网 **192.168.1.38** | ECS 上的独立容器 |
| 视频源 | **MinIO**（.38，mode 默认 minio） | **阿里云 OSS**（mode=oss，北京 zhishu-video-ai） |
| 后端启动 | `bash scripts/dev-run.sh` | `bash scripts/deploy/start.sh` |
| 小程序后端 | `mini/app.js` 中 `USE_PROD=false`（局域网） | `USE_PROD=true`（线上域名） |

- 线上部署的完整步骤见 [doc/DEPLOY.md](doc/DEPLOY.md)，排障见 [doc/TROUBLESHOOTING.md](doc/TROUBLESHOOTING.md)。
- 想在本地临时复现线上视频源：`bash scripts/dev-run-oss.sh`（走 OSS）。

## 运行

### 后端（本地：连 .38 的 MySQL/Nacos/Redis/MinIO）
```bash
# 环境首次：在 MySQL（192.168.1.38，root/root）建库建表灌种子
bash scripts/init-mysql.sh
# 准备本机密钥（gitignore）：填 MinIO 凭据，需要时填 OSS 凭据
# 然后一键启动（端口 8080）
bash scripts/dev-run.sh
# 不想依赖远程环境：SPRING_PROFILES_ACTIVE=h2 mvn -f backend/pom.xml spring-boot:run（H2 内存库）
```

### Web
```bash
cd web
npm install && npm run dev        # 端口 5173，/api 代理到 8080
```

### 微信小程序
用微信开发者工具导入 `mini/` 目录，并勾选「详情 → 本地设置 → 不校验合法域名」。
后端地址由 `mini/app.js` 的 `USE_PROD` 开关控制：本地开发保持 `false`
（局域网后端），上线时改为 `true`。

## 已实现接口
- Auth：`sms-code`(占位) `register` `login` `wechat-login`
- 内容：`/api/hot` `/api/categories` `/api/categories/{key}/videos` `/api/bloggers` `/api/bloggers/{id}/videos`
- 视频：`/api/videos/{id}`(详情含作者+收藏) `/api/videos/{id}/stream`
- 用户：`/api/user/profile` `/favorites`(列表/添加/删除) `/history`(列表/上报)

## 数据初始化
- **MySQL（默认）**：`scripts/init-mysql.sh` 建库 `zhishu`(utf8mb4) 并执行 `db/schema.sql` + `data.sql`（种子博主/分类/视频/文章）。需要本机有 JDK17，不需要 mysql 客户端；库中已有表时拒绝重跑，`INIT_FORCE=1` 可强制重建（会清空数据）。
- **H2**：切到 h2 profile 时自动执行同样的 schema/data 脚本，数据仅在进程内存中。

## 后续规划
- 真实短信服务替换 MockSmsService
- 域名 HTTPS 收尾、小程序正式提审
- 视频转码/封面增强、点赞评论、搜索、推荐
- 数据库迁移工具（Flyway）、小程序真实 AppID code2session

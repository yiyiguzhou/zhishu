# 变更记录（Changelog）

本文件是项目全部变更的**人工可读总结**（Git 提交历史的索引与说明）。
AI 每次变更上库时必须同步更新本文件（规则见 [CLAUDE.md](../CLAUDE.md)）。
原始逐行差异以 Git 历史为准：`git log` / `git show <sha>`。

**环境拓扑（当前）**

| 角色 | 地址 | 说明 |
|---|---|---|
| 应用服务器（后端/Web） | 192.168.1.175 | macOS，Spring Boot + React |
| 数据/网关机 | 192.168.1.38 | Mac mini：MySQL 8、Nacos 2.5.4、MinIO（数据目录本机 `~/Documents/YouTube`）、Redis 7（AOF，供验证码等共享状态） |
| NAS（可选，当前不使用） | 192.168.1.2 | WD My Cloud EX2 Ultra |

**里程碑 Tags**：`baseline-skeleton`（骨架基线）→ `arch-nacos`（Nacos）→ `data-mysql`（MySQL）→ `media-minio-gateway`（视频网关）→ `feature-learning-assistant`（学习助手）

---

## 2026-09-28 · 线上部署改造（分支 feat/prod-deploy，合入 v1 前定稿）

**背景**：平台准备上线阿里云，目标拓扑为 ECS（Nginx + 后端 + Nacos 容器）+ RDS MySQL + OSS 视频源，域名 ICP 备案后对公网仅暴露 80/443。

**变更内容**：

- 后端新增 **oss 视频源**：`OssMediaResolver` 对私有 bucket 生成 2 小时预签名 URL，客户端直连 OSS，视频流量不过后端；media_key 存储中立可原样迁移；`MEDIA_MODE=oss` 启用（57fe578）
- 生产配置全部环境变量化：`JWT_SECRET/WX_APP_ID/WX_SECRET/LOG_LEVEL/MEDIA_MODE/OSS_*`，本地默认值保持开发行为不变（49510ae）
- 部署编排：`deploy/backend/Dockerfile`（多阶段）、`deploy/docker-compose.prod.yml`（nacos+后端+nginx，MySQL 外接 RDS，另备 local-mysql profile）、`deploy/nginx/nginx.conf`（SPA、/api 反代、SSE 关 buffering、HTTP 跳 HTTPS）、`.env.prod.example`（eda66e4）
- 启动脚本 `scripts/deploy/`：`start.sh`（校验→构建 Web→镜像→起服务）、`stop/restart/logs.sh`、`init-remote-db.sh`（支持 RDS 与容器库）（4a845e4）
- 小程序 `baseUrl` 切换线上域名占位 `https://your-domain.com`（208a21a）
- 部署文档 `doc/DEPLOY.md`

**验证**：JDK17 `mvn compile` 通过（含 OSS SDK 依赖下载）；YAML 解析校验；脚本 `bash -n`；小程序 `node --check`。Web 无源码改动。**实际部署/真机与小程序 WXML 行为待 ECS 环境与微信开发者工具验证**。

**阶段一 · 高可用集群改造（同日追加）**，目标无单点：后端无状态多副本 + Nacos 集群 + RDS 高可用 + 云 Redis + SLB：

- 验证码改存 Redis（StringRedisTemplate，5 分钟过期），多实例共享发码/校验（7578711）
- 引入 actuator：`/actuator/health` 及 liveness/readiness 子探针；优雅停机 30s 宽限（c0b82ca）
- 编排支持 `--scale backend=N`：compose 加 redis 持久化、Nacos/Redis/MySQL 改 profile、
  Nginx 经 Docker DNS 动态解析后端；common.sh 统一 compose `--env-file` 插值（f33a040）
- Nacos 三节点集群编排（独立 MySQL 存 nacos_config，建表 SQL 从镜像提取，不入库）（87bff88）
- `doc/DEPLOY.md` 第 10 节：集群资源规划、Nacos 集群、应用 ECS 配置、SLB、验证与演进
- 验证：mvn compile、YAML、bash -n 通过；**实际故障切换/扩缩容行为待云上验证**
- 开发环境配套：.38 上新增 `zhishu-redis` 容器（redis:7-alpine，AOF，命名卷
  zhishu-redis-data，6379，无密码与开发默认值一致），本机实测 PING→PONG

---

## 2026-09-30 · 收藏改造：确认弹框 + 状态落库 + 软取消

- favorite 表新增 `status`（active/canceled）与 `canceled_at`；“未收藏”区分
  从未收藏（无行）与收藏后取消（行保留 canceled）；重新收藏复活原行不新增
- 后端：addFavorite 改为 upsert（插入/复活/幂等），removeFavorite 改软删除；
  收藏列表、favorited 状态、收藏计数均只查 active
- Web：详情页收藏按钮改 Popconfirm（取消/确认），仅确认写库
- 验证：无头浏览器 + 数据库实测 取消不落库、确认 active、软取消 canceled、
  重新收藏行数=1、个人中心卡片正常无报错

## 2026-09-30 · 新增日志功能（请求日志 + 业务关键节点）

- **请求层**：RequestLoggingFilter 记录方法/路径/状态码/耗时/登录用户，手机号脱敏（c87cd95）
- **业务节点**：
  - AuthService：注册/自动注册/登录/微信登录
  - ContentService：热门聚合、分类/博主取视频计数（info）；实体逐条转 DTO（debug）
  - UserService：收藏新增/幂等跳过/取消、历史新增/更新
  - AssistantService：模型流式/非流式调用起止、异常
  - Minio/OssMediaResolver：预签名生成（debug，仅记 bucket/key，不记 URL）
- 分级原则：业务事件 info、高频逐条转换 debug；密码/验证码/密钥/预签名 URL 不日志
- 验证：登录、收藏、聚合实测输出正确；debug 日志被 Nacos 远程 info 级别覆盖（符合预期）

## 2026-09-30 · 本地 / 线上环境正式切割

- 本地：后端经 `scripts/dev-run.sh` 启动，连 .38 的 MySQL/Nacos/Redis，
  视频走 MinIO（凭据从 gitignore 的 .dev-secrets 注入）；
  OSS 凭据同文件保留，dev-run-oss.sh 可临时复现线上视频源
- 线上：ECS 独立容器 + `.env.prod`，视频走 OSS；小程序 `USE_PROD` 开关区分两端
- README 新增「环境划分」章节并更新启动方式
- 验证：MinIO 预签名 206、Web 5173 与 API 200

## 2026-09-30 · 正式环境部署（进行中）· ECS 北京 182.92.124.62

**背景**：备案完成，上线阿里云。资源：ECS 2核2G + 40G（Alibaba Cloud Linux 3），
MySQL 8 自建容器（预算考虑，未购 RDS），视频走已有的北京 OSS `zhishu-video-ai`。
代码经 rsync 部署到 `/opt/zhishu`（GitHub 国内 clone 超时）。

**变更内容 / 排障记录**：

- 小规格适配：2G swap 兜底；Nacos JVM 256m（补 `JVM_XMN=128m`——镜像默认
  -Xmn512m 大于堆曾导致初始化静默失败）；MySQL 缓冲池 128m；后端限堆 384m；
  Dockerfile 构建走阿里云 Maven 镜像
- **中文双重编码修复**：data.sql 经 mysql 客户端导入时默认 latin1，中文变
  `æœ€ä¹‹`；init-remote-db.sh 统一加 `--default-character-set=utf8mb4`，
  重灌后字节校验正确
- **Nacos 2.5.4 变化**：镜像不再内置/自动初始化 `nacos` 管理员，启动即报
  "User nacos not found"。改为配置与用户存 MySQL 的 `nacos_config` 库
  （官方 mysql-schema.sql 建表），管理员手工 INSERT（bcrypt），
  启动后已用 API 改成强密码并同步 .env.prod
- Nginx 配置切换：compose bind 源不支持变量插值，改用 `nginx/active.conf`
  符号链接（当前指 nginx-http-only.conf，证书就绪后 ln 指 nginx.conf）
- **数据库自动备份**：backup-db.sh 每日 03:17 dump zhishu + nacos_config，
  gzip 上传 OSS `db-backups/`（已实测；建议配 OSS 生命周期过期）

**当前状态**：五容器（mysql/nacos/redis/backend/nginx）在 ECS 全部运行，
服务器本机访问页面/API 正常，后端注册 Nacos 健康，OSS 预签名播放此前已验证。
**外网仍不通**：安全组入方向仅放行 22，需控制台补 80/443；
域名尚在审核，证书与 HTTPS 切换待域名完成。（诊断时一度被本机 Clash TUN
本地代答误导，已用 check-host.net 外部节点确认。）
- 新增 [doc/TROUBLESHOOTING.md](TROUBLESHOOTING.md)：汇总开发与部署的 13 个
  问题（现象/根因/解决/验证）及排障方法论，DEPLOY.md 加入口链接
- 安全组 80/443 放行后外网全部打通：外部节点 /health 200，页面/API 200；
  助手 401 定位为 Nacos 占位符链未取到 Key（Key 直调方舟 200），
  改经 SPRING_AI_OPENAI_API_KEY 环境变量直注后 SSE 流式恢复正常

## 2026-09-29 · 新增业务与技术架构图

- `doc/architecture/` 新增 Mermaid 图源（业务 5 图 / 技术 7 图）及渲染出的 12 张 SVG，
  加 README 索引；内容反映最新状态（线上 OSS、Redis、Nacos 集群、SLB、本地 MinIO）

## 2026-09-29 · 视频迁移至阿里云 OSS（zhishu-video-ai，北京）

- OSS bucket 建在华北2-北京（原名 zhishu-video 被他人占用），私有、标准存储
- .38 装 ossutil 1.7.19（`~/bin`，校验和核对）；MinIO 数据目录是 xl.meta 分片格式，
  改用 pgsty 镜像内 mc 把 12 个真实对象 mirror 到暂存目录后上传 OSS（2.02 GiB）
- Bucket Policy：匿名仅允许 `oss:GetObject` 覆盖 `covers/*`；视频对象私有走预签名，
  ListObjects 匿名禁止；实测 6 张封面 200、视频/枚举 403
- video.cover 六行由 MinIO 地址改为 OSS 地址（UPDATE + data.sql 固化）；
  预签名播放链路此前已实测 206
- 排障：RAM 应用账户只授对象数据动作，故 HeadObject/Policy 类接口报 403（正常），
  策略管理用主账号；bucket 为"仅 Bucket Policy"模式，对象 ACL 禁用
- 本地后端切 OSS 模式实测通过：6 个视频经 OssMediaResolver 预签名播放全部 206，
  封面走 OSS 匿名地址；新增 `scripts/dev-run-oss.sh`（AK/SK 读 .dev-secrets，
  set -a 自动导出——直接 source 的变量不进 mvn 子进程，曾因此报 AK 空）

## 2026-09-28 · 修复小程序本地无视频（baseUrl 误指线上占位域名）

- 线上部署改造把 `mini/app.js` 的 baseUrl 直接改成 `https://your-domain.com`，
  合入 v1 后本地开发者工具请求全部失败、列表为空
- 改为 `USE_PROD` 开关：false 走局域网后端（192.168.1.175:8080，工具与真机同 Wi-Fi 可用），
  true 走线上域名；DEPLOY.md 第 9 节同步

## 2026-09-28 · 修复小程序登录后看不到学习助手入口

- 原因：五个页面仅在 json 注册了 chat-ball 组件，但 wxml 中未放置 `<chat-ball />` 标签
- 在 home/detail/favorites/history/user 五个页面 wxml 末尾补上标签，悬浮球正常显示

## 2026-09-28 · 未登录时隐藏学习助手入口

- 助手仅登录可用，未登录直接不渲染悬浮按钮（此前入口可见但提问静默无反应，造成困惑）
- 移除排查用的临时调试埋点

## 2026-09-28 · 修复助手在 Web 端无回复（Vite 代理 IPv4/IPv6 不匹配）

- 现象：浏览器提问无任何返回；后端 SSE 实际正常（直连 8080 有 18 帧增量）
- 原因：Vite 仅监听 IPv6 `::1`，代理目标写成 `localhost` 解析到 IPv4 127.0.0.1，
  上游连接失败（http 000），请求未到达后端
- 修复：vite proxy target 显式改为 `http://127.0.0.1:8080`；重启 Vite 后代理 SSE 正常

## 2026-09-28 · 大模型学习助手上线（tag: feature-learning-assistant）

- **后端**：`/api/assistant/chat`（SSE 流式）与 `/chat/sync`（整体返回）；
  登录强制校验、系统提示词、客户端多轮历史；RestClient 调火山方舟，无新增依赖
- **Web**：FloatButton 悬浮聊天窗（打字机、可停止、多轮上下文），全站挂载
- **小程序**：pages/chat + chat-ball 组件（五个页面注册）
- **模型/密钥最终方案**：模型 deepseek-v4-flash-260425；Ark key 配置在 Nacos ZHISHU_GROUP
  `zhishu-backend.yaml` 的 `spring.ai.openai.api-key`（单等号），不入库；
  不再使用 DEFAULT_GROUP 的 nanny-monitor-api-key.properties
- 排障记录：properties 双等号会使注入值带前导 =（Ark 报格式错误）；
  旧 key（46 位 ark-dc0e…）火山侧已失效，换新 key 后调通
- 验证：SSE 增量、sync 完整回复、未登录 401 全部实测通过

## 2026-09-27 · 视频首帧自动生成封面

- ffmpeg 从源 MP4 抽取首帧（1280 宽，JPEG ~50KB），上传至 bucket `covers/` 前缀
- covers 前缀设为 anonymous download（封面需免登录展示；视频播放仍走预签名）
- video.cover 更新 6 行，data.sql 固化；验证封面 URL 200

## 2026-09-27 · 视频按标题完成技术分类

- 新增 3 个技术类别：AI编程(ai-coding)、AI动态(ai-trends)、基础概念(fundamentals)，共 11 个分类
- 6 条视频归类：AI动态×1、智能体框架×1、RAG检索×1、基础概念×1、AI编程×2
- 作者仍留空待更新；验证详情类别名与按 key 过滤接口正常

## 2026-09-27 · 真实视频入库，mock 视频数据下线

- video 表 5 条 mock 记录替换为 **6 条真实视频记录**（对应 bucket zhishu-video 全部对象）
- 视频的 `blogger_id/category_id` 置空，由用户后续更新；video_tag 关联清空（tag 字典保留）
- **标题与文件名分离**：title 保留中文真实标题，media_key 改用规范英文名
  （ai-frontier.mp4 等）；起因是 MinIO/mc 对对象 key 中 `#` 字符支持异常
  （列表可见但 GET NoSuchKey、无法 rm/cp），英文 key 同时利于 OSS 迁移
- bucket 清理：删除含 # 的残留对象（xl 残留目录，主机层 rm），测试副本不再保留
- 验证：6 条视频预签名 URL 全部 GET 200、Range 206，首页展示真实标题
- 原始中文名 MP4 保留在网关机数据根（bucket 外），作为源文件备份

## 2026-09-27 · 视频源改用网关机本机目录（脱离 NAS）

- 本地调试不再挂 NAS：MinIO 数据目录改到 .38 本机 `~/Documents/YouTube`，
  compose 用 `MINIO_DATA_DIR` 参数化；NAS SMB 看门狗与 LaunchAgent 已从网关机移除
- 迁移完成：bucket zhishu-video 现有 7 个对象（6 个 YouTube MP4 + `rag/rag-full-guide.mp4`）
- 新增授权经验：首次 bind `~/Documents` 需在网关机弹窗中允许（TCC）
- 排障中 Docker 引擎 create/start 路径挂死（旧操作残留），全量重启 Docker Desktop 恢复
- 验证：预签名 URL GET 200（video/mp4）、Range 206
- README、技术架构文档同步

## 2026-09-27 · 网关 SMB 故障修复与自愈加固

- **故障**：NAS SMB 会话中途失效导致陈旧挂载（读写永久阻塞、Finder 手动连接卡在"正在连接"）；
  卡死的 I/O 使进程进入不可中断 U 态，软件层 umount 无效 → **重启 .38 恢复**
- 挂载改用 **soft 模式**（`mount_smbfs -o soft`）：连接故障时自动卸载而非永久挂死
- 网关挂载脚本升级为**看门狗**：挂载失败自动重试（NAS 恢复后挂载会间歇失败）、驻留检测丢失后
  自动重挂并重启 MinIO 恢复 /data 绑定（`deploy/minio/mount-nas.sh`），LaunchAgent 常驻
- 验证：GET 200、Range 206 播放正常
- 经验：容器删除/MinIO 容器重启是恢复 bind 挂载的必要步骤；排查中避免反复执行 lsof（会产生大量 U 态残留进程）

## 2026-09-27 · 架构文档整理

- 新增 `doc/architecture/technical-architecture.md`：三端架构总览、技术选型、后端分层、
  StreamSource/Nacos/认证/Profile 关键设计、部署拓扑、API 总览、核心链路、工程保障
- 新增 `doc/architecture/business-architecture.md`：产品定位、用户角色、业务模块、
  注册/播放/收藏流程、信息架构、业务数据模型、运营约定、业务规划
- 新增 `doc/README.md` 文档中心索引（`55ca4c4` 之后提交）

---

## 2026-09-26 · 视频存储网关上线（tag: `media-minio-gateway`）

**背景**：视频需要存放在 NAS 并可在线播放；架构决策为视频能力独立成"类 OSS 存储网关"，应用后端只做 S3 客户端。

**变更内容**：
- **修复 Web 播放器**：详情页 `<video>` 缺少 `src`，此前视频无法播放（`2d875d5`）
- **视频元数据规范化**：
  - video 表新增 `category_id` 外键指向类别表，`blogger_id` 补外键；删除松散的 `category_key` 列
  - 新增 `tag` 表与 `video_tag` 多对多关联表（外键级联）
  - 详情接口返回类别名与标签列表；Web 详情页渲染标签，小程序详情页同步展示（`02e4123`）
- **nas 模式（备选保留）**：新增 NasMediaResolver，后端本机直挂 NAS 共享；挂载脚本 + LaunchAgent（`10f7ae1`、`3a4c9e5`）
- **MinIO 网关**（最终方案，部署在 .38）：
  - 新增 `deploy/minio/` 工件：NAS 挂载脚本、登录自动挂载、docker-compose（`26224a4`）
  - 后端 mode 切换到 minio，endpoint 指向 192.168.1.38:9000，凭据走 `MINIO_ACCESS_KEY/SECRET_KEY`（`7935121`）
  - 视频 source_type 同步为 minio（data.sql + 线上 MySQL UPDATE 5 行，`3a0c6f6`）
  - 官方 minio/minio 镜像 2025-10 起停发并从 Docker Hub 下架，改用**逐行兼容的社区分支 pgsty/minio**（`390fe54`）
- 文档同步到网关架构（`7551c9e`、`accda40`）

**验证**：预签名 URL GET 200、Range 请求 **206**（拖动可用），浏览器实测可播。

**经验记录**：
- MinIO 密码强制 ≥8 位（用户 ≥3 位）
- 视频对象必须经 S3 API/9001 控制台上传；直接丢进 NAS 目录不会登记（SMB 无文件事件、xl 单盘无 heal）

---

## 2026-09-19 · 分支策略确立

- v1 确认为默认开发分支，GitHub 仓库默认分支切换为 v1（`8a10995`）
- 明确规则：绝不在 main 提交，main 仅作发布分支；大变更从 v1 切 `feat/` 分支（`82161ab`）

## 2026-09-19 · 默认数据库切换为 MySQL（tag: `data-mysql`）

- H2 内存库 → MySQL（192.168.1.38，库名 zhishu，utf8mb4），数据持久化（`61c6ecb`）
- 新增 `scripts/init-mysql.sh` + `InitMysql.java`：免 mysql 客户端一键建库建表灌种子；已有表拒绝重跑，`INIT_FORCE=1` 强制重建
- 验证：注册用户 → 重启后端 → 同账号可登录（持久化实测）；H2 模式保留（`SPRING_PROFILES_ACTIVE=h2`）

## 2026-09-17 · 后端接入 Nacos（tag: `arch-nacos`）

- Spring Cloud 2023.0.3 + Spring Cloud Alibaba 2023.0.3.4（nacos-client 2.4.3）
- 服务以 `zhishu-backend` 注册到 `ZHISHU_GROUP`；配置中心监听 `zhishu-backend.yaml`（optional）
- Nacos 不可用不阻断启动（`fail-fast=false`，自动重连）；新增 `deploy/nacos/docker-compose.yml`
- 验证：实例注册健康；远程改日志级别约 10 秒免重启动态生效

## 2026-09-16 · 三端骨架基线 + AI 开发规程（tag: `baseline-skeleton`）

- 三端骨架入库：backend（Spring Boot 3/Java 17）、web（React/Vite）、mini（原生小程序），共 118 文件（`e14bd86`）
- 建立 AI 开发规程：小步提交、先验证再提交、pre-commit 钩子（按需触发编译/类型检查）
- .gitignore 补充：忽略 TS 构建缓存 tsconfig.tsbuildinfo（`61da3c4`）

## 2026-09-16 · 项目初始化

- `9e8f986` Initial commit

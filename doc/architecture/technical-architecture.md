# 技术架构文档

> 纸书 · 大模型学习平台
> 最后更新：2026-09-27 · 对应版本：v1（tag `media-minio-gateway`）

## 1. 架构总览

平台采用 **三端 + 独立基础设施** 架构：

```
┌────────────── 客户端 ──────────────┐
│ Web（PC 浏览器）   微信小程序        │
└──────────────┬─────────────────────┘
               │ HTTPS/HTTP
┌──────────────▼───────────────────────────────────────┐
│ 应用服务器 192.168.1.175（macOS）                     │
│  Spring Boot 3 · JWT 鉴权 · StreamSource 视频源抽象    │
│  ├─ Controller 层（REST API）                         │
│  ├─ Service 层（业务编排）                            │
│  ├─ Mapper 层（MyBatis-Plus）                         │
│  └─ S3 客户端（MinIO SDK）                            │
└───────┬───────────────────────────────┬─────────────┘
        │ JDBC                           │ S3/预签名
┌───────▼───────────────┐      ┌──────────▼──────────────────────┐
│ 数据/网关机 .38        │      │ MinIO 网关（运行在 .38）          │
│ MySQL 8（zhishu 库）   │      │ S3 API :9000 / 控制台 :9001      │
│ Nacos 2.5.4           │◀─────│ 数据目录 bind 自本机磁盘目录       │
│ 注册/配置中心          │      │ ~/Documents/YouTube              │
└───────────────────────┘      └──────────────────────────────────┘

# NAS（WD My Cloud EX2 Ultra）保留为可选物理存储，当前本地调试不挂载
```

## 2. 技术选型

| 层 | 技术 | 版本 | 选型理由 |
|---|---|---|---|
| 后端框架 | Spring Boot | 3.3.4 / Java 17 | 主流成熟生态 |
| 微服务组件 | Spring Cloud Alibaba | 2023.0.3.4 | Nacos 注册/配置，nacos-client 2.4.3 |
| ORM | MyBatis-Plus | 3.5.7 | 单表 CRUD 零 SQL，Lambda 条件构造 |
| 数据库 | MySQL | 8.x | 生产/开发持久化；库名 zhishu、utf8mb4 |
| 开发数据库 | H2 | 随 Boot | 内存模式零外部依赖，MySQL 兼容方言 |
| 对象存储网关 | pgsty/minio | RELEASE.2026-06-18 | S3 兼容；官方 minio/minio 停发后的社区逐行兼容分支 |
| 网关数据目录 | 本机磁盘 | — | `~/Documents/YouTube`（macOS 需 TCC 授权） |
| 物理存储（可选） | WD My Cloud EX2 Ultra | — | NAS；本地调试当前不使用 |
| 认证 | JWT (jjwt) | 0.12.6 | 无状态 Token，有效期 7 天 |
| 学习助手 | 火山方舟（OpenAI 兼容） | deepseek-v4-flash-260425 | 大模型答疑，RestClient 调用；密钥经 Nacos 注入 |
| Web | React + TypeScript | 18 / Vite 5 | Ant Design 5 组件库、Zustand 状态管理 |
| 小程序 | 原生微信小程序 | — | WXML/WXSS/JS，无构建步骤 |

## 3. 后端分层架构

包路径根：`com.zhishu`

```
controller/   REST 入口，参数校验，返回统一结构 ApiResponse<T>
service/      业务逻辑与编排（ContentService / VideoService / AuthService / UserService）
  media/      视频源抽象层（见 4.1）
mapper/       MyBatis-Plus BaseMapper，单表操作
entity/       数据库实体（@TableName，下划线转驼峰）
dto/          接口出入参模型
config/       WebConfig、Properties 配置绑定（zhishu.* 前缀）
common/       ApiResponse、BusinessException、全局异常处理、JWT、拦截器、用户上下文
```

统一约定：

- 响应体 `{code, message, data}`，业务错误抛 `BusinessException`，由全局异常处理转响应（404/500 等）
- `AuthInterceptor` 拦截 `/api/**`（排除 `/api/auth/**`），解析 JWT 后放入 `UserContext`（ThreadLocal）

## 4. 关键技术设计

### 4.1 视频源抽象（StreamSource）

```java
public interface StreamSource {
    String resolvePlayUrl(Video video);
}
```

- 三个实现，由配置项 `zhishu.media.mode` 互斥选择（`@ConditionalOnProperty`）：

| mode | 实现 | playUrl 形态 | Range 支持 |
|---|---|---|---|
| `minio`（默认） | MinioMediaResolver | S3 预签名直链（2 小时有效），播放器直连网关 | MinIO 原生 206 |
| `nas` | NasMediaResolver | `/media/<media_key>`，校验挂载目录文件存在 | ResourceHttpRequestHandler 206 |
| `local` | LocalMediaResolver | `/media/<media_key>`，指向本机 media 目录 | 同上 |

- 播放不经手应用服务器字节流：minio 模式播放器直连网关；nas/local 模式 `/media/**` 由静态资源机制处理
- `media_key` 保持**存储中立**（如 `rag/rag-full-guide.mp4`）：现在是 NAS 内路径，上阿里云 OSS 后直接作为 Object Key
- **扩展新源**（OSS/Jellyfin/Plex）：新增一个实现 + mode 枚举值即可，Controller、Service、前端零改动
- 备选能力：`GET /api/videos/{id}/stream` 返回解析后的播放地址 JSON（当前前端未使用，预留）

### 4.2 Nacos 注册发现与配置中心

- 服务名 `zhishu-backend`，分组 `ZHISHU_GROUP`；实例注册本机 IP + 8080
- 配置中心监听（均 `optional`）：`zhishu-backend.yaml`（公共）、`zhishu-backend-<profile>.yaml`
- 配置变更约 10 秒推送，免重启（Spring RefreshEvent）
- 容错：`fail-fast=false`，Nacos 不可达不阻断启动，恢复后自动重连重注册
- 本机自建：`deploy/nacos/docker-compose.yml`（单机、开鉴权）

### 4.3 认证机制

| 端 | 方式 | 流程 |
|---|---|---|
| Web | 手机号 + 验证码 | `sms-code`（开发期占位码 `123456`）→ `register`/`login` → JWT |
| 小程序 | 微信快速登录 | `wx.login` 取 code → `wechat-login`（无 AppID 时 Mock 返回 openid） |

- JWT 经 `Authorization` 请求头传递；密钥/有效期配置在 `zhishu.jwt`
- `/media/**` 不在鉴权拦截范围（静态资源免登录）

### 4.4 大模型学习助手

- `POST /api/assistant/chat`（SSE 流式，Web 悬浮聊天窗）与 `/chat/sync`（整体返回，小程序 chat 页）
- 仅登录用户可用（`UserContext.require()`）；系统提示词固化在 AssistantService；多轮历史由客户端上送
- 使用 Boot 自带 RestClient 调用火山方舟 chat completions，无新增依赖；
  密钥 `spring.ai.openai.api-key` 配置在 Nacos ZHISHU_GROUP `zhishu-backend.yaml`
- 不引入 Spring AI：其 1.0 基线为 Boot 3.4，会连锁牵动 Spring Cloud/SCA 升级

### 4.5 数据访问与环境 Profile

| Profile | 数据库 | 表结构来源 |
|---|---|---|
| `mysql`（默认） | 192.168.1.38:3306/zhishu | 首次由 `scripts/init-mysql.sh` 建库建表灌种子；应用 `sql.init.mode=never` |
| `h2` | jdbc:h2:mem:zhishu（内存） | 每次启动自动执行 schema.sql + data.sql |

- `scripts/init-mysql.sh` + `scripts/InitMysql.java`：JDK17 源文件模式直连 MySQL，
  无需 mysql 客户端；库非空拒绝重跑，`INIT_FORCE=1` 强制重建
- MyBatis-Plus：`map-underscore-to-camel-case=true`，主键自增

## 5. 部署拓扑

| 主机 | 角色 | 关键端口/路径 |
|---|---|---|
| 192.168.1.175 | 应用服务器：Spring Boot（8080）、Vite（5173） | `/api`、`/media` |
| 192.168.1.38 | MySQL 8（3306）、Nacos（8848 HTTP / 9848 gRPC）、MinIO（9000 API / 9001 控制台） | 数据目录 `~/Documents/YouTube`（本机磁盘） |
| 192.168.1.2 | NAS（可选，当前不挂载） | SMB 445 |

## 6. API 总览

| 模块 | 接口 |
|---|---|
| 认证 | `POST /api/auth/sms-code` · `register` · `login` · `wechat-login` |
| 内容 | `GET /api/hot` · `/api/categories` · `/api/categories/{key}/videos` · `/api/bloggers` · `/api/bloggers/{id}/videos` |
| 用户 | 认证 |
| 视频 | `GET /api/videos/{id}` · `/api/videos/{id}/stream` |
| 学习助手 | `POST /api/assistant/chat`(SSE) · `/api/assistant/chat/sync` |
| 用户 | `GET /api/user/profile` |
| 收藏 | `GET/POST /api/user/favorites` · `DELETE /api/user/favorites/{type}/{id}` |
| 历史 | `GET /api/user/history` · `POST /api/user/history` |

## 7. 核心链路

**播放链路（minio 模式）**：
客户端请求 `/api/videos/{id}` → Service 查视频/作者/类别/标签 → MinioMediaResolver 生成预签名 URL → 播放器直连 `192.168.1.38:9000` 请求视频字节（支持 Range 206）。

**登录链路（Web）**：
请求验证码 → 登录接口校验 → 返回 JWT + 用户信息 → 前端存入 Zustand/localStorage → 后续请求带 Token。

**历史上报**：
详情页加载（已登录）→ `POST /api/user/history`（targetId/progress）→ upsert 历史记录。

## 8. 工程保障

- pre-commit 钩子：暂存区含 Java/TS/小程序 JS 时分别触发编译、tsc、node --check
- 一键验证：`bash scripts/verify.sh`
- 分支策略：v1 默认开发分支；大变更走 `feat/*`；main 仅发布
- 变更留痕：Git 小步提交 + `doc/CHANGELOG.md` 同步总结

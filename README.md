# 纸书 · 大模型学习平台

大模型学习平台的 **三端骨架**（Java 后端 / React Web / 微信小程序）。
当前为项目骨架：统一技术底座与核心数据模型已就位，功能页可跑通主链路，后续逐端填充完整功能。

## 目录结构

```
zhishu/
├── backend/   Spring Boot(Java 17) + MyBatis-Plus + H2/MySQL + JWT + MinIO 抽象
├── web/       React 18 + TypeScript + Vite + Ant Design + Zustand
└── mini/      原生微信小程序（WXML + JS）
```

## 核心设计

- **视频来源抽象 `StreamSource`**：后端可按 `zhishu.media.mode` 在 `local`(默认)/`minio` 间切换，
  接入 NAS / Jellyfin / Plex 直链只需新增一个 Resolver。
- **两种分类共用 category 表**：按技术(`video_tech`，harness/mcp/rag…) 或按博主(`blogger`)。
- **认证**：小程序微信快速登录(`wx.login`→openid)；Web 手机号+验证码（开发期占位码 `123456`）。

## 运行

### 后端（默认 H2 内存库，零配置）
```bash
cd backend
JAVA_HOME=/path/to/jdk17 mvn spring-boot:run     # 端口 8080
# 生产切 MySQL：--spring.profiles.active=mysql
```
> H2 需 Java17 编译：`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`

### Web
```bash
cd web
npm install && npm run dev        # 端口 5173，/api 代理到 8080
```

### 微信小程序
用微信开发者工具导入 `mini/` 目录，并勾选「详情-不校验合法域名」。
本地后端地址在 `mini/app.js` 的 `globalData.baseUrl`（默认 `http://127.0.0.1:8080`）。

## 已实现接口
- Auth：`sms-code`(占位) `register` `login` `wechat-login`
- 内容：`/api/hot` `/api/categories` `/api/categories/{key}/videos` `/api/bloggers` `/api/bloggers/{id}/videos`
- 视频：`/api/videos/{id}`(详情含作者+收藏) `/api/videos/{id}/stream`
- 用户：`/api/user/profile` `/favorites`(列表/添加/删除) `/history`(列表/上报)

## 数据初始化
H2 自动执行 `backend/src/main/resources/db/schema.sql` + `data.sql`，含种子博主/分类/视频/文章。

## 后续规划（骨架已预留）
- 真实短信服务替换 MockSmsService
- NAS/Jellyfin/Plex 直链 Resolver
- 视频转码/封面、点赞评论、搜索、推荐
- 生产 MyBatis 迁移(Flyway)、小程序真实AppID code2session

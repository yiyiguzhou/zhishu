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

## 核心设计

- **视频来源抽象 `StreamSource`**：后端按 `zhishu.media.mode` 在 `local`/`minio`/`nas` 间切换
  （线上预留 `oss`）；接入 NAS/Jellyfin/Plex/OSS 只需新增一个 Resolver。
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

## 视频存储网关（MinIO + NAS）

视频文件在 NAS（WD My Cloud EX2 Ultra，`192.168.1.2`），但**不由应用服务器直挂**：
由独立网关机（`192.168.1.38`）挂载 NAS 并运行 MinIO，对外提供 S3/类 OSS API；
zhishu 后端只是 S3 客户端，解析出预签名播放 URL，播放器直连网关。

```
NAS(.2) ─SMB─▶ .38 挂载点 ─bind─▶ MinIO(:9000 API / :9001 控制台)
zhishu-backend(.175, mode=minio) ─预签名URL─▶ Web/小程序播放器直连 .38:9000
```

**① NAS 管理页（http://192.168.1.2）**
1. 新建共享 `zhishu`，建用户（如 `zhishu`）并授予读写
2. 共享内建目录 `zhishu-video/`（即 bucket），其下按 `rag/`、`harness/`、`mcp/` 分类；
   文件路径与 video.media_key 对应（`zhishu-video/rag/rag-full-guide.mp4` ↔ key `rag/rag-full-guide.mp4`）；
   浏览器播放要求 MP4/H.264/AAC

**② 网关机 .38（工件在 `deploy/minio/`）**
```bash
# 凭据文件 ~/.config/zhishu/nas.env（chmod 600）：NAS_IP/NAS_SHARE/NAS_USER/NAS_PASSWORD
bash deploy/minio/mount-nas.sh                    # 挂载 NAS 到 ~/mnt/zhishu（免 sudo）
bash deploy/minio/install-mount-autostart.sh     # 登录自动挂载（可选）
cp deploy/minio/.env.example deploy/minio/.env   # 设置 MinIO 管理员账号密码
docker compose -f deploy/minio/docker-compose.yml up -d
# 控制台 http://192.168.1.38:9001 ；可建专用 service account 给后端用
```

**③ 应用服务器 .175**：启动时注入网关凭据（不入库）：
`MINIO_ENDPOINT`（默认已指向 .38）、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`、`MINIO_BUCKET`(默认 zhishu-video)

- 经 SMB 直接放进目录的文件在 MinIO 单盘模式下立即可见；上传也可走 9001 控制台
- 三种模式按 `zhishu.media.mode` 切换：`minio`(默认，网关) / `nas`(后端本机直挂，脚本在 `scripts/`) / `local`
- 上阿里云 OSS 时 media_key 直接复用为 Object Key，仅需改 endpoint/凭据并新增 oss Resolver

## 运行

### 后端（默认 MySQL + MinIO 网关，数据持久化）
```bash
# 环境首次：在 MySQL（默认 192.168.1.38，root/root）建库建表灌种子
bash scripts/init-mysql.sh
# 启动（端口 8080）；MinIO 网关需已在 .38 运行，凭据走环境变量
cd backend
MINIO_ACCESS_KEY=xxxx MINIO_SECRET_KEY=xxxx \
  JAVA_HOME=/path/to/jdk17 mvn spring-boot:run
# MySQL：MYSQL_HOST/MYSQL_USER/MYSQL_PASSWORD/MYSQL_DB
# 不想依赖远程环境：SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run（H2 内存库，重启即重置）
```

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
- **MySQL（默认）**：`scripts/init-mysql.sh` 建库 `zhishu`(utf8mb4) 并执行 `db/schema.sql` + `data.sql`（种子博主/分类/视频/文章）。需要本机有 JDK17，不需要 mysql 客户端；库中已有表时拒绝重跑，`INIT_FORCE=1` 可强制重建（会清空数据）。
- **H2**：切到 h2 profile 时自动执行同样的 schema/data 脚本，数据仅在进程内存中。

## 后续规划（骨架已预留）
- 真实短信服务替换 MockSmsService
- 线上阿里云 OSS 视频源（OssMediaResolver，凭据走 Nacos）
- 视频转码/封面、点赞评论、搜索、推荐
- 生产 MyBatis 迁移(Flyway)、小程序真实AppID code2session

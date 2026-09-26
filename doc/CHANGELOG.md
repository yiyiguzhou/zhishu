# 变更记录（Changelog）

本文件是项目全部变更的**人工可读总结**（Git 提交历史的索引与说明）。
AI 每次变更上库时必须同步更新本文件（规则见 [CLAUDE.md](../CLAUDE.md)）。
原始逐行差异以 Git 历史为准：`git log` / `git show <sha>`。

**环境拓扑（当前）**

| 角色 | 地址 | 说明 |
|---|---|---|
| 应用服务器（后端/Web） | 192.168.1.175 | macOS，Spring Boot + React |
| 数据/网关机 | 192.168.1.38 | Mac mini：MySQL 8、Nacos 2.5.4、MinIO 网关（pgsty/minio） |
| NAS | 192.168.1.2 | WD My Cloud EX2 Ultra，zhishu 共享（bucket 目录 zhishu-video） |

**里程碑 Tags**：`baseline-skeleton`（骨架基线）→ `arch-nacos`（Nacos）→ `data-mysql`（MySQL）→ `media-minio-gateway`（视频网关）

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

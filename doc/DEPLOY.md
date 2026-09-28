# 线上部署指南（阿里云）

本文说明纸书平台上线的完整步骤。部署相关文件均在 `deploy/`，启动脚本在 `scripts/deploy/`。

## 1. 架构与资源清单

```
域名（已备案）→ ECS：Nginx 80/433（唯一对公网开放）
                    ├─ 静态 Web（dist 挂载进 Nginx 容器）
                    ├─ /api → 后端容器 :8080
                    └─ 播放地址由客户端直连 OSS（预签名 URL）
后端容器 → RDS MySQL（内网）、Nacos 容器（本机容器网络）
```

| 资源 | 规格建议 | 说明 |
|---|---|---|
| ECS | 2 核 4G + 40G 盘，Ubuntu 22.04 | 与 RDS 同一 VPC；视频流量不过 ECS |
| RDS MySQL | 8.0 单机基础版 | 也可用 compose 的 `local-mysql` profile 同机部署 |
| OSS | 标准存储私有 bucket | 视频源；可选绑 CDN |
| 域名 | 已 ICP 备案 | 备案约 1~2 周，最先启动 |

安全组只放行 **80、443**；3306/8848/9848/9000/8080 均不对公网开放。

## 2. 服务器初始化

```bash
# Docker（compose 插件随 docker-ce 一起安装）
curl -fsSL https://get.docker.com | sudo bash

# Node.js 20（start.sh 构建 Web 用；也可在本机构建后上传 web-dist）
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo bash -
sudo apt install -y nodejs rsync default-mysql-client git

git clone -b feat/prod-deploy <仓库地址> zhishu && cd zhishu
```

## 3. 数据库

**RDS**：创建实例时把 ECS 内网 IP 加入白名单，建账号（如 `zhishu`）。

```bash
MYSQL_HOST=rm-xxxx.mysql.rds.aliyuncs.com MYSQL_USER=root \
MYSQL_PASSWORD=xxxx bash scripts/deploy/init-remote-db.sh
```

该脚本建库 + 执行 `db/schema.sql` + `db/data.sql`（含 DROP TABLE，仅首次/重置时执行）。
如需把开发库现有数据整体带走，改用 `mysqldump --databases zhishu` 导出导入。

**同机 MySQL（备选）**：compose 加 profile，再用容器方式初始化：

```bash
docker compose -f deploy/docker-compose.prod.yml --profile local-mysql up -d mysql
USE_CONTAINER=1 MYSQL_PASSWORD=xxxx bash scripts/deploy/init-remote-db.sh
```

## 4. 环境变量

```bash
cp deploy/.env.prod.example deploy/.env.prod
```

必须填写/替换：

- `NACOS_AUTH_TOKEN`：`openssl rand -base64 48`
- `NACOS_PASSWORD`、`MYSQL_URL/USER/PASSWORD`、`JWT_SECRET`（`openssl rand -base64 48`）
- 视频源保持 `MEDIA_MODE=oss`，填 OSS endpoint、RAM 用户 AK/SK、bucket
- `WX_APP_ID/WX_SECRET`、`SPRING_AI_OPENAI_API_KEY`（火山方舟；也可改放 Nacos）

## 5. Nacos

```bash
docker compose -f deploy/docker-compose.prod.yml up -d nacos
```

控制台经 SSH 隧道访问（不开放公网）：

```bash
ssh -L 8848:127.0.0.1:8848 <ECS 用户>@<ECS 公网IP>
# 浏览器开 http://127.0.0.1:8848/nacos，首登改掉 nacos/nacos
```

密钥放环境变量即可，Nacos 非必须再建配置；如需集中管理，在 `ZHISHU_GROUP`
建 `zhishu-backend.yaml` 覆盖对应键。

## 6. OSS 与视频迁移

1. 控制台建私有 bucket（与 RDS 同地域），创建仅授权 `AliyunOSSFullAccess`（建议收窄到该 bucket）的 RAM 用户，拿 AK/SK。
2. `OSS_ENDPOINT` 填 bucket 对外域名（默认 `https://oss-cn-<地域>.aliyuncs.com`，或 CDN/自定义域名）。预签名 URL 的主机名取自该值，必须与客户端实际访问地址一致。
3. 上传视频，**对象 key 与数据库 `media_key` 保持一致**（沿用 MinIO bucket 中的目录结构）：

```bash
# 本机安装 ossutil 后
ossutil cp -r ~/Documents/YouTube/ oss://zhishu-video/
```

封面若要匿名展示，可对 `covers/` 前缀单独设公共读。

## 7. 证书与域名

- DNS：域名 A 记录指向 ECS 公网 IP。
- 证书：申请阿里云免费 DV 证书（或其他 SSL 证书），下载 Nginx 格式后放到：
  `deploy/certs/zhishu.pem`、`deploy/certs/zhishu.key`。
- 全局替换占位域名：

```bash
grep -rl your-domain.com deploy mini | xargs sed -i '' 's/your-domain.com/实际域名/g'
```

## 8. 一键启动

```bash
bash scripts/deploy/start.sh
```

脚本会校验 `.env.prod` 和证书、构建 Web 并同步到 `deploy/web-dist/`、构建后端镜像、起全部容器。
其他命令：`scripts/deploy/{stop,restart,logs}.sh [服务名]`。

验证：浏览器开 `https://实际域名`，检查列表、详情播放（预签名）、学习助手流式回复、登录。

## 9. 小程序上线

1. `mini/app.js` 的 `baseUrl` 已指向线上域名（随第 7 步替换）。
2. 微信公众平台：完成小程序备案；"开发管理 → 服务器域名"添加：
   - request 合法域名：`https://实际域名`
   - downloadFile 合法域名：OSS/CDN 域名（`<video>` 播放预签名地址用）
3. 微信开发者工具上传 → 提交审核 → 发布。

## 10. 常见问题

- **助手回复整段卡住/一次蹦出全部**：Nginx 对 `/api/assistant/chat` 必须 `proxy_buffering off`，已在配置中处理，自定义改动时勿删。
- **播放地址 403 / SignatureDoesNotMatch**：多为后端 `OSS_ENDPOINT` 与实际访问主机不一致，或服务器时钟漂移（`timedatectl` 检查 NTP）。
- **Nginx 容器启动失败**：多为证书文件未就位，`start.sh` 会提前检查。
- **Nacos 连不上**：后端容器经 `nacos:8848` 访问（compose 内部网络）；Nacos 不阻断启动，但密钥若放 Nacos 需确保先就绪。

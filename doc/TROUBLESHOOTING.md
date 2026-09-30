# 问题记录与排障手册（Troubleshooting）

纸书平台在开发与正式部署过程中实际遇到的问题汇总。每条包含：现象、根因、
解决方式、验证/预防。新问题按分类追加。配套文档：[DEPLOY.md](DEPLOY.md)。

> 最后更新：2026-09-30

## 索引

| # | 问题 | 分类 |
|---|---|---|
| 1 | 中文标题双重编码（æœ€ä¹‹ 乱码） | 数据库 |
| 2 | Nacos 启动报 User nacos not found | 中间件 |
| 3 | Nacos JVM 年轻代大于堆，初始化静默失败 | 中间件 |
| 4 | BCrypt 哈希经 ssh 双引号被 `$` 展开破坏 | 运维操作 |
| 5 | Compose bind 挂载源不支持变量插值 | 运维操作 |
| 6 | Nginx 502：变量 proxy_pass 缺少 resolver | Web 服务 |
| 7 | 本机 Clash TUN 代答导致端口"假可连" | 网络环境 |
| 8 | 安全组未放行 80/443，外网访问超时 | 云资源 |
| 9 | 视频预签名报 Access Key Id 不存在 | 对象存储 |
| 10 | Nginx 未关 buffering，SSE 回复整段卡住 | Web 服务 |
| 11 | Vite 代理 IPv4/IPv6 不匹配，Web 助手无回复 | 本地开发 |
| 12 | 小程序 baseUrl 误指占位域名，列表无视频 | 小程序 |
| 13 | 小规格 ECS 内存不足风险（2G） | 云资源 |
| 14 | 助手 401：Key 在 Nacos 但后端占位符未取到 | 中间件 |

---

## 数据库

### 1. 中文标题双重编码（æœ€ä¹‹ 乱码）

- **现象**：API 返回标题形如 `AIæœ€å‰æ²¿çš„äºº`；库里 HEX 看到
  `4149C3A6C593...`（"最"的 UTF-8 字节 `E69C80` 被再编码了一次）。
- **根因**：`data.sql` 是 UTF-8 文件，但 mysql 客户端导入时**连接字符集默认
  latin1**，服务端按 latin1 接收字节再转存 utf8mb4，造成双重编码。
- **解决**：`scripts/deploy/init-remote-db.sh` 的所有 mysql 调用统一加
  `--default-character-set=utf8mb4`，重跑 schema + data。
- **验证/预防**：导入后 `SELECT HEX(SUBSTRING(title,1,6))` 应为
  `4149E69C80E5`（AI最前）。任何经管道/重定向喂 SQL 的场景都要显式指定字符集。

---

## 中间件

### 2. Nacos 启动报 "User nacos not found"

- **现象**：控制台/接口登录返回 `user not found!`；后端启动日志
  `Error getting properties from nacos ... 403 user not found`。
- **根因**：**Nacos 镜像 2.5.4 不再内置、也不自动初始化默认 `nacos/nacos`
  管理员**（与 2.4 以前镜像行为不同），users 表为空。
- **解决**：Nacos 配置与用户改存 MySQL 的 `nacos_config` 库：
  1. 建库并从镜像提取 `mysql-schema.sql` 建表；
  2. 手工 INSERT 管理员（bcrypt 哈希，见问题 4）和 `ROLE_ADMIN`；
  3. compose 用 `MYSQL_SERVICE_*` 环境变量指向该库；
  4. 启动后立即用 `/nacos/v1/auth/users` API 改成强密码并同步 `.env.prod`。
- **验证/预防**：登录接口返回 accessToken；不要按旧文档假设默认账号存在。
  详细步骤见 [DEPLOY.md 第 5 节](DEPLOY.md)。

### 3. Nacos JVM 年轻代大于堆，初始化静默失败

- **现象**：容器能启动，但管理员/认证初始化未完成（叠加问题 2 时一度怀疑
  是它导致用户创建被跳过）。
- **根因**：把 Nacos 堆压到 `JVM_XMX=256m`，但镜像默认 `-Xmn512m`
  （年轻代），**年轻代比整个堆还大**，JVM 处于非法配置，部分初始化静默失败。
- **解决**：compose 显式加 `JVM_XMN=128m`（必须 < XMX）。
- **验证/预防**：压缩 JVM 堆时 Xms/Xmx/Xmn 三个参数必须一起核对。

---

## 运维操作

### 4. BCrypt 哈希经 ssh 双引号被 `$` 展开破坏

- **现象**：UPDATE 管理员密码后，库里 `LENGTH(password)` 只有 32
  （应为 60），值形如 `a0.Qha1GZf...`，登录始终失败。
- **根因**：通过 `ssh "mysql -e \"... '$2a$10$...'\""` 传递 SQL 时，
  哈希中的 `$2a`、`$10`、`$<片段>` 被**远程 shell 当环境变量展开**，值被掏空。
- **解决**：含 `$` 的值一律用**标准输入**传 SQL：
  `printf "UPDATE ... '%s'" "$HASH" | ssh ... 'docker exec -i mysql mysql ...'`。
- **验证/预防**：写入后立即查长度/内容；bcrypt、含 `$` 的密钥禁止走
  双引号 -e 方式。

### 5. Compose bind 挂载源不支持变量插值

- **现象**：写了 `- ${NGINX_CONF_FILE}:/etc/nginx/conf.d/default.conf:ro`，
  即使显式 `NGINX_CONF_FILE=xxx`，`docker compose config` 渲染出的源路径
  仍固定为默认值。
- **根因**：Compose 在 bind 挂载的短语法里对该插值支持不稳定（与 `.env`/
  `--env-file` 的插值时机有关）。
- **解决**：放弃变量，固定挂载 `./nginx/active.conf`，宿主机用
  **符号链接**在 `nginx.conf`（HTTPS）和 `nginx-http-only.conf`（临时 HTTP）
  之间切换：`ln -sfn nginx-http-only.conf active.conf`。
- **验证/预防**：切换后 `ls -la active.conf` 确认指向，重建容器生效。

---

## Web 服务（Nginx）

### 6. 502 Bad Gateway：变量形式 proxy_pass 缺少 resolver

- **现象**：Nginx 起来了，但所有 `/api/` 返回 502。
- **根因**：为动态解析后端副本，`proxy_pass http://$backend:8080` 用了变量，
  但临时配置里**漏配 `resolver`**，Nginx 无法经 Docker 内嵌 DNS 解析主机名。
- **解决**：配置顶部加 `resolver 127.0.0.11 valid=10s ipv6=off;`
  （Docker 用户定义网络的内嵌 DNS）。
- **验证/预防**：`proxy_pass` 一旦含变量就必须有 resolver。

### 10. SSE 回复整段卡住 / 一次蹦出全部

- **现象**：学习助手提问后长时间无输出，最后整段一起出现。
- **根因**：Nginx 默认开启 `proxy_buffering`，把上游 SSE 帧攒在缓冲区。
- **解决**：对 `/api/assistant/chat` 单独配 `proxy_buffering off; proxy_cache off;`，
  并把 `proxy_read_timeout` 调大。
- **验证/预防**：已在 nginx 配置中处理，自定义改动时勿删该 location 块。

---

## 网络环境

### 7. 本机 Clash TUN 代答导致端口"假可连"

- **现象**：`nc -z 182.92.124.62 <任意端口>` 全部显示成功，连**根本没有
  发布的 6555/12345/33333 也"可连"**；但 HTTP 请求 5 秒后 Empty reply，
  tcpdump 在 ECS 网卡上**抓不到任何包**。
- **根因**：本机 Clash Verge 的 **TUN 模式**在本地协议栈代答了 SYN，
  HTTP 流量被代理绕走，数据包从未离开本机；误判成"安全组已放行/备案拦截"。
- **解决**：不改用户代理配置，改用**外部网络视角**验证
  （check-host.net 的 TCP/HTTP 检查，全球多节点）。
- **验证/预防**：本机开 TUN 代理时，`nc`/`telnet` 的端口探测结论不可信；
  涉及安全组、公网可达性一律用外部节点或第三方服务确认。

### 8. 安全组未放行 80/443，外网访问超时

- **现象**：经 check-host.net 外部节点测 80/443，全部 Connection timed out；
  22 正常（外部节点可连，0.3s）。
- **根因**：ECS 安全组入方向只放行了 22，缺少 80/443 规则。
- **解决**：控制台安全组入方向添加 80、443，授权对象 `0.0.0.0/0`；
  3306/6379/8848/9848/8080 均不开（仅 compose 内网使用）。
- **验证/预防**：改完再用外部节点 TCP 检查确认（注意问题 7 的陷阱）。

---

## 对象存储

### 9. 视频预签名报 "The Access Key Id you provided does not exist"

- **现象**：列表、封面正常，视频详情接口 500，报 MinIO AccessKey 不存在。
- **根因**：.38 的 MinIO 容器实际凭据是部署时 `.env`（不入库）里的
  `wzhang/wzhang1234`，而 application.yml 默认值是 `minioadmin`；
  封面走匿名读不受影响，视频预签名才暴露。
- **解决**：本地启动后端带 `MINIO_ACCESS_KEY=wzhang MINIO_SECRET_KEY=wzhang1234`；
  已固化到项目记忆与启动方式。
- **验证/预防**：预签名 Range 请求返回 206。凭据与代码默认值不一致是
  常见现象，密钥类一律以部署环境实际值为准。

---

## 本地开发

### 11. Vite 代理 IPv4/IPv6 不匹配，Web 助手无回复

- **现象**：浏览器提问无任何返回；直连 8080 的 SSE 正常。
- **根因**：Vite 仅监听 IPv6 `::1`，proxy target 写 `localhost` 解析到
  IPv4 `127.0.0.1`，上游连不上。
- **解决**：[vite.config.ts](../web/vite.config.ts) proxy target 显式写
  `http://127.0.0.1:8080`。
- **验证/预防**：本机服务间调用尽量用显式 IP，不用依赖 localhost 解析。

### 12. 小程序 baseUrl 误指占位域名，列表无视频

- **现象**：微信开发者工具首页视频全部消失。
- **根因**：线上改造把 `mini/app.js` 的 baseUrl 直接写成占位域名
  `https://your-domain.com`（不存在），合并后本地请求全部失败。
- **解决**：改为 `USE_PROD` 开关：`false` 走局域网后端
  `http://192.168.1.175:8080`，`true` 走线上域名。
- **验证/预防**：本地小程序开发始终保持 `USE_PROD=false` 并勾选
  "不校验合法域名"；上线前才切 true。

### 13. 小规格 ECS 内存不足风险（2核2G）

- **现象/风险**：2G 机器同时跑 MySQL + Nacos(JVM) + 后端(JVM) + Redis +
  Nginx，峰值内存吃紧，有被 OOM Killer 杀进程的风险。
- **应对**（用户明确选择 2G 方案）：
  - 加 **2G swap**（/swapfile，写入 fstab）兜底；
  - Nacos 堆 256m、MySQL `innodb_buffer_pool_size=128m`、后端限堆 384m；
  - 视频字节流走 OSS/预签名，不占 ECS 内存。
- **验证/预防**：`free -h` 观察；后续若加后端副本或流量上涨，优先升级到
  4G/8G。数据库长期建议迁 RDS。

---

## 中间件（续）

### 14. 助手 401：Key 在 Nacos 里但后端没取到

- **现象**：Nacos `zhishu-backend.yaml` 中已配 `spring.ai.openai.api-key`，
  后端日志也显示 Load config success，但调助手返回
  `模型服务调用失败：401 UNAUTHORIZED`；把同一个 Key 从服务器直调方舟
  却返回 200。
- **根因**：后端实际读取的是 `zhishu.assistant.api-key`，其值为占位符
  `${spring.ai.openai.api-key:}`。该跨前缀占位符在配置绑定阶段未能可靠解析到
  Nacos 远程配置中的值，`properties.getApiKey()` 实际为空，请求未带凭据。
- **解决**：Key 直接以后端环境变量 `SPRING_AI_OPENAI_API_KEY` 注入
  （写入 `.env.prod`，compose 经 env_file 注入），不依赖 Nacos 占位符链。
- **验证/预防**：重建后端后 SSE 正常流式；密钥类配置优先用环境变量直注，
  少用"占位符引用另一前缀"的间接写法。

---

## 排障方法论小结

1. **先分层定位**：客户端 → Nginx → 后端 → 中间件 → 网络/安全组，逐层 curl/抓包。
2. **警惕本机环境干扰**：代理（TUN）、hosts、IPv4/IPv6、localhost 解析。
3. **外部视角验证公网结论**：check-host.net 等第三方节点。
4. **保留证据**：改配置前记录现状，验证用字节/状态码而不是"看起来对"。
5. **含 `$`、密钥类操作**：走标准输入/环境变量，不走双引号命令行。

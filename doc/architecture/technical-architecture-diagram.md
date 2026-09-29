# 技术架构图（纸书 · 大模型学习平台）

> 2026-09-29 · 配套文档：[technical-architecture.md](technical-architecture.md)、部署指南：[../DEPLOY.md](../DEPLOY.md)

## 1. 总体技术架构（线上正式环境）

```mermaid
flowchart TB
    subgraph Clients["客户端"]
        Web["Web 浏览器<br/>React 18 + TS + Vite"]
        Mini["微信小程序<br/>原生 WXML/JS"]
    end

    DNS["域名（已 ICP 备案）"]
    SLB["SLB 负载均衡<br/>80 / 443"]

    subgraph ECS["ECS（华北2-北京）"]
        Nginx["Nginx 容器<br/>静态 Web + 反代 + SSE"]
        subgraph Backends["后端无状态副本"]
            B1["zhishu-backend 1"]
            B2["zhishu-backend 2…"]
        end
    end

    subgraph Infra["云基础设施（VPC 内网）"]
        RDS[("RDS MySQL 8.0<br/>高可用版")]
        Nacos["Nacos 集群 ×3<br/>注册 / 配置中心"]
        Redis[("Redis 主从<br/>验证码 / 锁 / 缓存")]
    end

    OSS["阿里云 OSS<br/>zhishu-video-ai（私有）<br/>covers 公共读"]
    Ark["火山方舟<br/>学习助手大模型"]

    Web --> DNS --> SLB --> Nginx
    Mini --> DNS
    Mini --> SLB
    Nginx --> B1 & B2
    B1 & B2 --> RDS
    B1 & B2 --> Nacos
    B1 & B2 --> Redis
    B1 & B2 -- "SSE 转发调用" --> Ark
    B1 & B2 -- "生成预签名" --> OSS
    Web -. "播放/封面直连" .-> OSS
    Mini -. "播放/封面直连" .-> OSS
```

## 2. 本地开发环境

```mermaid
flowchart TB
    Dev["开发机 192.168.1.175<br/>Vite :5173 / Spring Boot :8080"]

    subgraph GW["数据/网关机 192.168.1.38"]
        MySQL[("MySQL 8 :3306")]
        Nacos["Nacos :8848/9848"]
        Minio["MinIO :9000/9001<br/>数据目录 ~/Documents/YouTube"]
        Redis[("Redis :6379")]
    end

    Dev --> MySQL
    Dev --> Nacos
    Dev --> Redis
    Dev -- "minio 模式（默认）" --> Minio
    Dev -. "oss 模式：dev-run-oss.sh" .-> OSS["阿里云 OSS（北京）"]
```

## 3. 后端分层架构

```mermaid
flowchart TB
    subgraph App["com.zhishu（Spring Boot 3 / Java 17）"]
        C["controller 层<br/>REST 入口 · 参数校验 · ApiResponse"]
        S["service 层<br/>业务编排"]
        M["mapper 层<br/>MyBatis-Plus BaseMapper"]
        subgraph Media["service/media 视频源抽象"]
            SS["«interface» StreamSource"]
            LM["LocalMediaResolver"]
            MM["MinioMediaResolver"]
            OM["OssMediaResolver"]
            NM["NasMediaResolver"]
            SS --> LM & MM & OM & NM
        end
        E["entity / dto / config / common"]
        C --> S --> M
        S --> Media
    end
    M --> DB[("MySQL")]
    MM --> MinioS["MinIO"]
    OM --> OssS["OSS"]
```

## 4. 视频源抽象（StreamSource）

```mermaid
flowchart LR
    Props["zhishu.media.mode<br/>（MEDIA_MODE 环境变量）"]
    Props -->|local| L["LocalMediaResolver<br/>/media/{mediaKey}"]
    Props -->|minio 默认| M["MinioMediaResolver<br/>MinIO 预签名 2h"]
    Props -->|oss 线上| O["OssMediaResolver<br/>OSS 预签名 2h"]
    Props -->|nas| N["NasMediaResolver<br/>/media 挂载目录"]
    M & O --> URL["playUrl 直链（支持 Range 206）<br/>字节流不经后端"]
```

## 5. 线上高可用部署拓扑

```mermaid
flowchart TB
    subgraph AZ["跨可用区"]
        subgraph ECS1["ECS 1"]
            N1["Nginx"] --> A1["backend"]
        end
        subgraph ECS2["ECS 2"]
            N2["Nginx"] --> A2["backend"]
        end
    end
    SLB["SLB"] --> N1 & N2

    A1 & A2 --> N1N["Nacos 1"]
    A1 & A2 --> N2N["Nacos 2"]
    A1 & A2 --> N3N["Nacos 3"]
    N1N & N2N & N3N --> NDB[("Nacos 集群 MySQL")]

    A1 & A2 --> RDSM[("RDS 主")]
    RDSM -.主备同步.-> RDSS[("RDS 备")]
    A1 & A2 --> RM[("Redis 主")]
    RM -.-> RS[("Redis 从")]
```

## 6. 核心链路：视频播放

```mermaid
sequenceDiagram
    participant U as 客户端
    participant N as Nginx
    participant V as VideoService
    participant R as OssMediaResolver
    participant O as OSS

    U->>N: GET /api/videos/{id}
    N->>V: 转发请求（JWT 鉴权）
    V->>V: 查视频/作者/类别/标签
    V->>R: resolvePlayUrl(video)
    R-->>V: OSS 预签名 URL（2h）
    V-->>N: VideoDetailDTO
    N-->>U: 返回详情（含 playUrl）
    U->>O: GET 对象（Range）
    O-->>U: 206 视频字节（直连，不经后端）
```

## 7. 核心链路：登录与历史上报

```mermaid
sequenceDiagram
    participant U as 客户端
    participant A as AuthService
    participant Redis as Redis
    participant DB as MySQL

    U->>A: POST /api/auth/sms-code
    A->>Redis: SET sms:code:{phone}（5 min）
    U->>A: POST /api/auth/login（手机号+验证码）
    A->>Redis: GET 验证码比对
    A->>DB: 查/建用户
    A-->>U: JWT + 用户信息
    Note over U: 后续请求 Authorization: Bearer JWT

    U->>A: POST /api/user/history（进度）
    A->>DB: upsert 观看记录
```

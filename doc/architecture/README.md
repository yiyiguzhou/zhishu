# 架构文档与图索引

| 文档 | 内容 |
|---|---|
| [business-architecture.md](business-architecture.md) | 业务架构文档（文字版） |
| [technical-architecture.md](technical-architecture.md) | 技术架构文档（文字版） |
| [business-architecture-diagram.md](business-architecture-diagram.md) | 业务架构图源（Mermaid） |
| [technical-architecture-diagram.md](technical-architecture-diagram.md) | 技术架构图源（Mermaid） |

## 业务架构图（SVG）

| 图 | 文件 |
|---|---|
| 业务能力地图 | [business-architecture-diagram-1.svg](business-architecture-diagram-1.svg) |
| 核心业务流程 | [business-architecture-diagram-2.svg](business-architecture-diagram-2.svg) |
| 内容三种到达维度 | [business-architecture-diagram-3.svg](business-architecture-diagram-3.svg) |
| 信息架构（页面结构） | [business-architecture-diagram-4.svg](business-architecture-diagram-4.svg) |
| 业务数据模型 ER | [business-architecture-diagram-5.svg](business-architecture-diagram-5.svg) |

## 技术架构图（SVG）

| 图 | 文件 |
|---|---|
| 总体技术架构（线上） | [technical-architecture-diagram-1.svg](technical-architecture-diagram-1.svg) |
| 本地开发环境 | [technical-architecture-diagram-2.svg](technical-architecture-diagram-2.svg) |
| 后端分层架构 | [technical-architecture-diagram-3.svg](technical-architecture-diagram-3.svg) |
| 视频源抽象 StreamSource | [technical-architecture-diagram-4.svg](technical-architecture-diagram-4.svg) |
| 线上高可用部署拓扑 | [technical-architecture-diagram-5.svg](technical-architecture-diagram-5.svg) |
| 播放链路时序 | [technical-architecture-diagram-6.svg](technical-architecture-diagram-6.svg) |
| 登录与历史上报时序 | [technical-architecture-diagram-7.svg](technical-architecture-diagram-7.svg) |

SVG 由 Mermaid 源文件渲染（`@mermaid-js/mermaid-cli`）；修改图时改对应的
`*-diagram.md` 后重新导出。

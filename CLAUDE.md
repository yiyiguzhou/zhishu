# CLAUDE.md — 纸书 · 大模型学习平台

本文件是 AI 协作者（Claude Code 等）在本仓库工作时必须遵守的规程。用户的口头要求优先于本文件；除此之外按此执行。

## 项目概览

AI 学习内容的视频平台，三端结构（详见 README.md）：

- `backend/` Spring Boot 3 (Java 17) + Spring Cloud Alibaba（Nacos 注册发现/配置中心）+ MyBatis-Plus + MySQL(默认，开发库 192.168.1.38:3306，root/root，库名 zhishu)/H2 内存库(`SPRING_PROFILES_ACTIVE=h2`) + JWT；表结构与种子数据在 `backend/src/main/resources/db/{schema,data}.sql`。MySQL 建库建表用 `bash scripts/init-mysql.sh`（应用自身不自动建表）。Nacos 开发环境在局域网 `192.168.1.38:8848`（账号 nacos/nacos，分组 `ZHISHU_GROUP`），不可用时不阻断启动；本机自建 Nacos 用 `deploy/nacos/docker-compose.yml`
- `web/` React 18 + TypeScript + Vite + Ant Design + Zustand
- `mini/` 原生微信小程序（WXML + JS），无构建步骤，只能在微信开发者工具中验证

## 本地开发命令

- 后端（必须 JDK 17，系统默认 Maven 挂的是 JDK 26，会失败）：
  `cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run`，端口 8080，默认连 192.168.1.38 的 MySQL；新环境首次先跑 `bash scripts/init-mysql.sh` 建库建表
- Web：`cd web && npm install && npm run dev`，端口 5173（用 http://localhost:5173，Vite 只监听 IPv6）
- 小程序：微信开发者工具导入 `mini/`；`mini/app.js` 的 `globalData.baseUrl` 指向后端
- 一键验证：`bash scripts/verify.sh`（后端编译 + Web 类型检查）

## AI 开发规程（核心）

目标：**每次变更都有记录、有问题随时能回退**。Git 历史就是变更账本。

1. **小步变更，一次只做一件事**。一个逻辑改动对应一个提交；不要把多个不相关的改动塞进一个提交。
2. **动手前先看 `git status`**。存在不属于本次任务的未提交改动时，先向用户确认，不要把别人的改动一起提交。
3. **先验证，再提交**，禁止把跑不通的代码提交进历史：
   - 后端改动：`JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn -f backend/pom.xml -q compile`（接口行为改动要实际请求验证，后端在跑时先重启）
   - Web 改动：`cd web && npx tsc -b`，交互改动尽量用浏览器实际打开验证
   - 小程序改动：`node --check` 验证 JS 语法；WXML/页面行为无法命令行验证，在交付说明中明确告知需要微信开发者工具验证
4. **提交信息**用中文，格式：
   ```
   <feat|fix|refactor|docs|chore|test>: <一句话说明做了什么>

   <必要时说明为什么、关键取舍、验证方式>
   ```
   提交信息结尾固定追加：
   ```
   Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>
   ```
5. **默认开发分支是 `v1`**（GitHub 仓库默认分支同为 v1）。**绝不在 `main` 上开发或直接提交**——main 只作发布/稳定分支，仅在发版节点由 v1 合入。新会话先 `git branch --show-current` 确认在 v1 上。大变更（新功能、跨端改动、有风险的重构）一律从 v1 切分支 `feat/<名称>`，完成验证后合回 v1；小修复可直接提交 v1。绝不在一条提交里混入"半成品"。
6. **修不好时**：保持改动未提交并如实报告卡在哪里，不要提交、不要用 reset/clean 清理现场，让用户决定。
7. **不主动 push**，除非用户明确要求；不主动执行 `git push -f`、`git reset --hard`、`git clean` 等不可逆操作。
8. 每次会话结束前，工作区应是干净的（改动已提交），或明确列出未提交内容及原因。

## 回退指南（给用户）

- 看变更历史：`git log --oneline`
- 撤销某次提交、但保留历史（安全，推荐）：`git revert <commit-sha>`
- 丢弃工作区里还没提交的改动：`git checkout -- <文件>`（不可恢复，先 `git stash` 更稳）
- 回到骨架基线：`git reset --hard baseline-skeleton`（会丢弃之后所有提交，执行前确认）
- 里程碑用打 tag 标记：`git tag <名称>`，基线 tag 为 `baseline-skeleton`

## 钩子（可选但已默认安装）

`scripts/git-hooks/pre-commit` 会在暂存了对应端代码时自动跑编译/类型检查。
重新克隆仓库后执行一次 `bash scripts/install-hooks.sh` 重新安装。

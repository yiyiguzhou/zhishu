# Web 镜像：构建静态文件 + Nginx 托管。构建上下文为仓库根目录。
FROM node:20-alpine AS build
WORKDIR /web
# 国内构建走淘宝 npm 镜像
RUN npm config set registry https://registry.npmmirror.com
COPY web/package.json web/package-lock.json* ./
RUN npm ci --no-audit
COPY web/ ./
RUN npm run build

FROM nginx:1.27-alpine
# 镜像内 Nginx 配置：静态托管 + API 反代 backend 容器
COPY deploy/nginx/web-image.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html
EXPOSE 80

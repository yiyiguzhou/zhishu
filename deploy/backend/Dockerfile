# 后端镜像（多阶段构建）。构建上下文为仓库根目录，见 deploy/docker-compose.prod.yml
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# 先单独拷贝 pom 预热依赖层
COPY backend/pom.xml ./pom.xml
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

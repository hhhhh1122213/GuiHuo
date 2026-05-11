# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 配置阿里云 Maven 镜像，加速 jar 包下载
COPY settings.xml /root/.m2/settings.xml

# 复制项目文件，一步打包
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# ===========================
# partner-file-service 文件服务 Dockerfile
# ===========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 设置时区为上海
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制打包好的 jar 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8083

# JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动应用，激活 docker 配置
# 同时激活 local（OSS 密钥）和 docker 两个配置
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=local,docker -jar app.jar"]

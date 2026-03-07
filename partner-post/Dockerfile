# ===========================
# partner-post 帖子服务 Dockerfile
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
EXPOSE 8082

# JVM 参数（适配 2GB 小内存服务器）
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:MaxDirectMemorySize=64m"

# 启动应用，激活 docker 配置
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=Asia/Shanghai -Dspring.profiles.active=docker -jar app.jar"]

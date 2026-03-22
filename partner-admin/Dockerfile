# 使用 Eclipse Temurin JDK 21 作为基础镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制构建好的 jar 包
COPY target/*.jar app.jar

# 设置时区为上海
ENV TZ=Asia/Shanghai

# 暴露端口
EXPOSE 8084

# 启动命令，激活 docker 配置
ENTRYPOINT ["java", "-Xms128m", "-Xmx192m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-Dspring.profiles.active=docker", "-jar", "app.jar"]

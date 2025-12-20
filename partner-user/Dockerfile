# 使用多阶段构建优化镜像大小
# 第一阶段: 构建
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段: 运行
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 从构建阶段复制 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

# 启动参数
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV NACOS_ADDR="nacos:8848"
ENV MYSQL_HOST="mysql"
ENV MYSQL_PORT="3306"
ENV MYSQL_DATABASE="partner_finder"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
    -Dspring.cloud.nacos.discovery.server-addr=$NACOS_ADDR \
    -Dspring.cloud.nacos.config.server-addr=$NACOS_ADDR \
    -Dspring.datasource.url=jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai \
    -jar app.jar"]

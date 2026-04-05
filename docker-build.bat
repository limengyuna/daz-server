@echo off
chcp 65001 >nul
title 找搭子 - Docker 一键构建并启动

echo ============================================
echo     找搭子系统 - Docker 一键构建并启动
echo ============================================
echo.

set PROJECT_DIR=%~dp0

:: ===========================
:: 第一步: 检查 Docker 是否安装
:: ===========================
echo [检查] 正在检查 Docker 环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker！请先安装 Docker Desktop。
    echo        下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo [OK] Docker 已安装
echo.

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker 未启动！请先打开 Docker Desktop 并等待其启动完成。
    pause
    exit /b 1
)
echo [OK] Docker 已启动
echo.

:: ===========================
:: 第二步: Maven 编译打包所有服务
:: ===========================
echo [1/3] 正在编译打包所有服务 (mvn clean package) ...
echo       这可能需要几分钟，请耐心等待...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] Maven 打包失败！请检查代码是否有编译错误。
    pause
    exit /b 1
)
echo [OK] 所有服务打包完成
echo.

:: ===========================
:: 第三步: 停止旧容器
:: ===========================
echo [2/4] 正在停止旧容器...
cd /d "%PROJECT_DIR%"
docker-compose down 2>nul
echo [OK] 旧容器已停止
echo.

:: ===========================
:: 第四步: 构建 Docker 镜像
:: ===========================
echo [3/4] 正在构建 Docker 镜像...
cd /d "%PROJECT_DIR%"
docker-compose build
if %errorlevel% neq 0 (
    echo [错误] Docker 镜像构建失败！
    pause
    exit /b 1
)
echo [OK] Docker 镜像构建完成
echo.

:: ===========================
:: 第五步: 启动所有容器
:: ===========================
echo [4/4] 正在启动所有容器...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [错误] 容器启动失败！
    pause
    exit /b 1
)
echo.

echo ============================================
echo   所有服务已成功启动！
echo ============================================
echo.
echo   服务访问地址:
echo   - 网关 (统一入口):      http://localhost:8080
echo   - 用户服务:              http://localhost:8081
echo   - 帖子服务:              http://localhost:8082
echo   - 文件服务:              http://localhost:8083
echo   - Milvus 向量数据库:     localhost:19530
echo   - Sentinel 控制台:       http://localhost:8858  (账号密码: sentinel/sentinel)
echo   - Zipkin 链路追踪:       http://localhost:9411
echo.
echo   常用命令:
echo   - 查看运行状态:  docker-compose ps
echo   - 查看日志:      docker-compose logs -f
echo   - 停止所有服务:  docker-compose down
echo   - 重启某个服务:  docker-compose restart partner-user
echo ============================================
echo.
pause

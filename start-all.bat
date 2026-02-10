@echo off
chcp 65001 >nul
title 找搭子 - 服务启动器

echo ============================================
echo     找搭子系统 - 一键启动所有微服务
echo ============================================
echo.

set PROJECT_DIR=%~dp0

echo [1/5] 正在编译公共模块 partner-common ...
cd /d "%PROJECT_DIR%partner-common"
call mvn install -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] partner-common 编译失败！请检查代码。
    pause
    exit /b 1
)
echo [OK] partner-common 编译完成
echo.

echo [2/5] 启动 partner-gateway (网关服务) ...
start "partner-gateway" cmd /k "cd /d %PROJECT_DIR%partner-gateway && mvn spring-boot:run"

echo [3/5] 启动 partner-user (用户服务) ...
start "partner-user" cmd /k "cd /d %PROJECT_DIR%partner-user && mvn spring-boot:run"

echo [4/5] 启动 partner-post (帖子服务) ...
start "partner-post" cmd /k "cd /d %PROJECT_DIR%partner-post && mvn spring-boot:run"

echo [5/5] 启动 partner-file-service (文件服务) ...
start "partner-file-service" cmd /k "cd /d %PROJECT_DIR%partner-file-service && mvn spring-boot:run"

echo.
echo ============================================
echo   所有服务已在独立窗口中启动！
echo   关闭对应窗口即可停止对应服务
echo ============================================
echo.
pause

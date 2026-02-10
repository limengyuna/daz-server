@echo off
chcp 65001 >nul
title 找搭子 - 选择性启动

echo ============================================
echo     找搭子系统 - 选择性启动微服务
echo ============================================
echo.
echo   1. partner-gateway   (网关服务)
echo   2. partner-user      (用户服务)
echo   3. partner-post      (帖子服务)
echo   4. partner-file-service (文件服务)
echo   5. 启动全部服务
echo   0. 退出
echo.
echo ============================================

set /p choice=请选择要启动的服务 (可多选, 用空格分隔, 如: 1 3): 

set PROJECT_DIR=%~dp0

echo.
echo [准备] 正在编译公共模块 partner-common ...
cd /d "%PROJECT_DIR%partner-common"
call mvn install -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] partner-common 编译失败！
    pause
    exit /b 1
)
echo [OK] partner-common 编译完成
echo.

for %%i in (%choice%) do (
    if "%%i"=="1" (
        echo [启动] partner-gateway ...
        start "partner-gateway" cmd /k "cd /d %PROJECT_DIR%partner-gateway && mvn spring-boot:run"
    )
    if "%%i"=="2" (
        echo [启动] partner-user ...
        start "partner-user" cmd /k "cd /d %PROJECT_DIR%partner-user && mvn spring-boot:run"
    )
    if "%%i"=="3" (
        echo [启动] partner-post ...
        start "partner-post" cmd /k "cd /d %PROJECT_DIR%partner-post && mvn spring-boot:run"
    )
    if "%%i"=="4" (
        echo [启动] partner-file-service ...
        start "partner-file-service" cmd /k "cd /d %PROJECT_DIR%partner-file-service && mvn spring-boot:run"
    )
    if "%%i"=="5" (
        echo [启动] 全部服务 ...
        start "partner-gateway" cmd /k "cd /d %PROJECT_DIR%partner-gateway && mvn spring-boot:run"
        start "partner-user" cmd /k "cd /d %PROJECT_DIR%partner-user && mvn spring-boot:run"
        start "partner-post" cmd /k "cd /d %PROJECT_DIR%partner-post && mvn spring-boot:run"
        start "partner-file-service" cmd /k "cd /d %PROJECT_DIR%partner-file-service && mvn spring-boot:run"
    )
    if "%%i"=="0" (
        exit /b 0
    )
)

echo.
echo ============================================
echo   选中的服务已在独立窗口中启动！
echo ============================================
echo.
pause

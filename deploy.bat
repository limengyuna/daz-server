@echo off
chcp 65001 >nul
title 找搭子 - 一键部署到服务器

echo ============================================
echo     找搭子系统 - 一键部署到服务器
echo ============================================
echo.

:: ===========================
:: 服务器配置 (根据需要修改)
:: ===========================
set SERVER_IP=45.192.97.24
set SERVER_USER=root
set SERVER_DIR=/root/dazi-app/docker
set PROJECT_DIR=%~dp0

:: ===========================
:: 第一步: 停止服务器上旧的 jar 进程
:: ===========================
echo [1/5] 正在停止服务器上旧的 jar 进程...
ssh %SERVER_USER%@%SERVER_IP% "echo '正在查找并停止旧服务...' && pkill -f 'partner-gateway-0.0.1-SNAPSHOT.jar' 2>/dev/null; pkill -f 'partner-user-0.0.1-SNAPSHOT.jar' 2>/dev/null; pkill -f 'partner-post-0.0.1-SNAPSHOT.jar' 2>/dev/null; pkill -f 'partner-file-service-0.0.1-SNAPSHOT.jar' 2>/dev/null; pkill -f 'partner-admin-0.0.1-SNAPSHOT.jar' 2>/dev/null; sleep 2; echo '旧服务已停止'"
echo [OK] 旧服务已停止
echo.

:: ===========================
:: 第二步: Maven 打包
:: ===========================
echo [2/5] 正在编译打包所有服务...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] Maven 打包失败！请检查代码。
    pause
    exit /b 1
)
echo [OK] 打包完成
echo.

:: ===========================
:: 第三步: 在服务器上创建目录
:: ===========================
echo [3/5] 正在服务器上创建部署目录...
ssh %SERVER_USER%@%SERVER_IP% "mkdir -p %SERVER_DIR%/partner-gateway/target %SERVER_DIR%/partner-user/target %SERVER_DIR%/partner-post/target %SERVER_DIR%/partner-file-service/target %SERVER_DIR%/partner-admin/target"
if %errorlevel% neq 0 (
    echo [错误] SSH 连接失败！请检查网络或 SSH 配置。
    echo        确保已配置 SSH 免密登录，或手动输入密码。
    pause
    exit /b 1
)
echo [OK] 目录创建完成
echo.

:: ===========================
:: 第四步: 上传文件到服务器（并行上传，加速部署）
:: ===========================
echo [4/5] 正在并行上传文件到服务器...

:: 先串行上传 docker-compose.yml（很小，瞬间完成）
scp "%PROJECT_DIR%docker-compose.yml" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/docker-compose.yml

:: 清理旧的标志文件
del /q "%TEMP%\upload_*.done" 2>nul

:: 并行上传5个服务（每个服务在后台执行）
echo       [并行] 上传 partner-gateway ...
start /b cmd /c "scp "%PROJECT_DIR%partner-gateway\Dockerfile" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-gateway/Dockerfile && scp "%PROJECT_DIR%partner-gateway\target\partner-gateway-0.0.1-SNAPSHOT.jar" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-gateway/target/ && echo done > "%TEMP%\upload_gateway.done""

echo       [并行] 上传 partner-user ...
start /b cmd /c "scp "%PROJECT_DIR%partner-user\Dockerfile" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-user/Dockerfile && scp "%PROJECT_DIR%partner-user\target\partner-user-0.0.1-SNAPSHOT.jar" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-user/target/ && echo done > "%TEMP%\upload_user.done""

echo       [并行] 上传 partner-post ...
start /b cmd /c "scp "%PROJECT_DIR%partner-post\Dockerfile" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-post/Dockerfile && scp "%PROJECT_DIR%partner-post\target\partner-post-0.0.1-SNAPSHOT.jar" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-post/target/ && echo done > "%TEMP%\upload_post.done""

echo       [并行] 上传 partner-file-service ...
start /b cmd /c "scp "%PROJECT_DIR%partner-file-service\Dockerfile" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-file-service/Dockerfile && scp "%PROJECT_DIR%partner-file-service\target\partner-file-service-0.0.1-SNAPSHOT.jar" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-file-service/target/ && echo done > "%TEMP%\upload_file.done""

echo       [并行] 上传 partner-admin ...
start /b cmd /c "scp "%PROJECT_DIR%partner-admin\Dockerfile" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-admin/Dockerfile && scp "%PROJECT_DIR%partner-admin\target\partner-admin-0.0.1-SNAPSHOT.jar" %SERVER_USER%@%SERVER_IP%:%SERVER_DIR%/partner-admin/target/ && echo done > "%TEMP%\upload_admin.done""

:: 等待所有上传完成
echo       等待所有上传任务完成...
:wait_uploads
timeout /t 2 /nobreak >nul
if not exist "%TEMP%\upload_gateway.done" goto wait_uploads
if not exist "%TEMP%\upload_user.done" goto wait_uploads
if not exist "%TEMP%\upload_post.done" goto wait_uploads
if not exist "%TEMP%\upload_file.done" goto wait_uploads
if not exist "%TEMP%\upload_admin.done" goto wait_uploads

:: 清理标志文件
del /q "%TEMP%\upload_*.done" 2>nul

echo [OK] 所有文件上传完成
echo.


:: ===========================
:: 第五步: 在服务器上构建并启动
:: ===========================
echo [5/5] 正在服务器上构建镜像并启动容器...
ssh %SERVER_USER%@%SERVER_IP% "cd %SERVER_DIR% && docker compose down 2>/dev/null; docker compose up -d --build"
if %errorlevel% neq 0 (
    echo [错误] 服务器上 Docker 启动失败！
    pause
    exit /b 1
)
echo.

echo ============================================
echo   部署成功！
echo ============================================
echo.
echo   服务访问地址:
echo   - 网关 (统一入口):      http://%SERVER_IP%:8080
echo   - 用户服务:              http://%SERVER_IP%:8081
echo   - 帖子服务:              http://%SERVER_IP%:8082
echo   - 文件服务:              http://%SERVER_IP%:8083
echo   - 管理员服务:            http://%SERVER_IP%:8084
echo   - Sentinel 控制台:       http://%SERVER_IP%:8858  (账号密码: sentinel/sentinel)
echo   - Zipkin 链路追踪:       http://%SERVER_IP%:9411
echo.
echo   服务器上的管理命令:
echo   - 查看状态:  cd %SERVER_DIR% ^&^& docker-compose ps
echo   - 查看日志:  cd %SERVER_DIR% ^&^& docker-compose logs -f
echo   - 停止服务:  cd %SERVER_DIR% ^&^& docker-compose down
echo   - 重启服务:  cd %SERVER_DIR% ^&^& docker-compose restart
echo ============================================
echo.
pause

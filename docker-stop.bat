@echo off
chcp 65001 >nul
title 找搭子 - Docker 停止所有服务

echo ============================================
echo     找搭子系统 - 停止所有 Docker 容器
echo ============================================
echo.

cd /d "%~dp0"
docker-compose down

echo.
echo [OK] 所有容器已停止
echo.
pause

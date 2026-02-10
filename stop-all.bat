@echo off
chcp 65001 >nul
title 找搭子 - 服务停止器

echo ============================================
echo     找搭子系统 - 一键停止所有微服务
echo ============================================
echo.

echo 正在停止所有微服务...
echo.

taskkill /fi "WINDOWTITLE eq partner-gateway*" /f >nul 2>&1
echo [OK] partner-gateway 已停止

taskkill /fi "WINDOWTITLE eq partner-user*" /f >nul 2>&1
echo [OK] partner-user 已停止

taskkill /fi "WINDOWTITLE eq partner-post*" /f >nul 2>&1
echo [OK] partner-post 已停止

taskkill /fi "WINDOWTITLE eq partner-file-service*" /f >nul 2>&1
echo [OK] partner-file-service 已停止

echo.
echo ============================================
echo   所有服务已停止！
echo ============================================
echo.
pause

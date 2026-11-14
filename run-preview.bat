@echo off
title PortfolioPro - Instant Preview
echo ======================================================================
echo   PortfolioPro - Instant Preview & Verification Server
echo   Running on http://localhost:8080
echo ======================================================================

start http://localhost:8080

where python >nul 2>nul
if %ERRORLEVEL% equ 0 (
    python dev_server.py
    goto end
)

where node >nul 2>nul
if %ERRORLEVEL% equ 0 (
    node dev_server.js
    goto end
)

echo [ERROR] Neither Python nor Node.js were detected to run instant preview.
echo Please run 'run.bat' with Java or 'run-spring.bat' with Maven.
pause

:end
pause


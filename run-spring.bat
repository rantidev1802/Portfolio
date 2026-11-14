@echo off
title PortfolioPro - Spring Boot Runner
echo ======================================================================
echo   PortfolioPro: Spring Boot Web Application
echo ======================================================================

where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven (mvn) was not found in your PATH.
    echo You can install Maven or run via IntelliJ / VS Code by opening this folder.
    echo Alternatively, run run-preview.bat for instant preview!
    pause
    exit /b 1
)

echo Starting PortfolioPro via Spring Boot Maven Plugin...
start http://localhost:8080
mvn spring-boot:run
pause


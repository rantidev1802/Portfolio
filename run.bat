@echo off
title PortfolioPro - Java Standalone Server
echo ======================================================================
echo   PortfolioPro: Stock Trading & Portfolio Management Platform
echo ======================================================================
echo Checking Java installation...

where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not found in your PATH.
    echo Please install Java 11 or higher (or Java 17/21) from https://adoptium.net/
    pause
    exit /b 1
)

echo Compiling Java source files...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\main\java\com\portfoliopro\model\*.java src\main\java\com\portfoliopro\service\*.java src\main\java\com\portfoliopro\PortfolioProServer.java

if %ERRORLEVEL% neq 0 (
    echo [NOTICE] If using Java 11+, trying direct source execution...
    java src\main\java\com\portfoliopro\PortfolioProServer.java
    if %ERRORLEVEL% neq 0 (
        echo [INFO] Falling back to companion runner...
        python dev_server.py
    )
) else (
    echo Starting PortfolioPro Java Server...
    start http://localhost:8080
    java -cp bin com.portfoliopro.PortfolioProServer
)
pause


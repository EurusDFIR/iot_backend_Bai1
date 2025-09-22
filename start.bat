@echo off
echo 🚀 IoT Backend - Docker All-in-One Setup
echo =========================================

REM Check Docker
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker not found. Please install Docker Desktop first.
    echo 🔗 Download: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker not running. Please start Docker Desktop.
    pause
    exit /b 1
)

echo ✅ Docker is ready

REM Stop any existing containers
echo 🛑 Stopping existing containers...
docker-compose down -v

REM Build and start all services
echo 🏗️  Building and starting all services...
echo    📦 PostgreSQL Database
echo    📡 Mosquitto MQTT Broker
echo    🌱 Spring Boot IoT Backend
echo.
echo ⏳ This may take 2-3 minutes for first build...

docker-compose up --build -d

REM Wait for services to be healthy
echo ⏳ Waiting for all services to be ready...

set max_attempts=30
set attempt=0

:wait_loop
docker-compose ps --filter status=running --filter health=healthy | findstr "iot-backend-app" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ All services are running and healthy!
    goto success
)

set /a attempt+=1
if %attempt% GEQ %max_attempts% (
    echo ❌ Services took too long to start. Check logs:
    echo    docker-compose logs iot-backend
    pause
    exit /b 1
)

echo    Still waiting... (%attempt%/%max_attempts%)
timeout /t 10 /nobreak >nul
goto wait_loop

:success
echo.
echo 🎉 SUCCESS! IoT Backend is running!
echo ==================================
echo 📱 Web API: http://localhost:8080
echo 📊 Database: localhost:5432 (iotdb/iotuser/secret)
echo 📡 MQTT: localhost:1883
echo.
echo 🧪 Quick test:
echo curl http://localhost:8080/api/devices
echo.
echo 🔧 Useful commands:
echo    View logs:    docker-compose logs -f
echo    Stop all:     docker-compose down
echo    Restart:      docker-compose restart
echo    Clean reset:  docker-compose down -v ^&^& docker-compose up -d
echo.
pause
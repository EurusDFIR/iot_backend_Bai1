@echo off
echo 🚀 IoT Backend - Simple Docker Setup
echo =====================================

REM Check Docker
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker not found. Please install Docker Desktop first.
    pause
    exit /b 1
)

echo ✅ Docker is ready

REM Clean up
echo 🛑 Cleaning up existing containers...
docker-compose down -v

REM Build and start
echo 🏗️ Building and starting services...
docker-compose up --build -d

REM Simple wait
echo ⏳ Waiting for services to start (60 seconds)...
timeout /t 60 /nobreak >nul

REM Check status
echo 📋 Checking service status...
docker-compose ps

echo.
echo 🎉 Setup complete! Services should be running.
echo ==========================================
echo 📱 Web API: http://localhost:8080
echo 📊 Database: localhost:5432
echo 📡 MQTT: localhost:1883
echo.
echo 🧪 Test API:
echo curl http://localhost:8080/api/devices
echo.
echo 🔧 Useful commands:
echo   View logs:    docker-compose logs -f
echo   Stop all:     docker-compose down
echo   Restart:      docker-compose restart
echo.
pause
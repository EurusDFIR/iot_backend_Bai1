@echo off
echo.
echo 🚀 IoT Backend - Simple Start (No Health Checks)
echo =================================================
echo.

REM Check Docker
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker not found. Please install Docker Desktop first.
    pause
    exit /b 1
)

echo ✅ Docker found
echo.

echo 🛑 Cleaning up any existing containers...
docker-compose -f docker-compose-simple.yml down -v

echo.
echo 🚀 Starting simple version (no health checks)...
echo    📦 PostgreSQL Database
echo    📡 Mosquitto MQTT Broker  
echo    🌱 Spring Boot IoT Backend
echo.
echo ⏳ This may take 2-3 minutes...
echo.

REM Start with simple compose file
docker-compose -f docker-compose-simple.yml up --build -d

echo.
echo ⏳ Waiting 90 seconds for all services...
timeout /t 90 /nobreak >nul

echo.
echo 📋 Service Status:
docker-compose -f docker-compose-simple.yml ps

echo.
echo 🧪 Testing API...
curl -s http://localhost:8080/api/devices >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ SUCCESS! API is responding!
) else (
    echo ⚠️  API not ready yet. Check logs:
    echo    docker-compose -f docker-compose-simple.yml logs iot-backend
)

echo.
echo 🌐 Available Services:
echo    📱 Web API: http://localhost:8080/api/devices
echo    📊 Database: localhost:5432 (iotdb/iotuser/secret)  
echo    📡 MQTT: localhost:1883
echo.
echo 🔧 Useful commands:
echo    View logs: docker-compose -f docker-compose-simple.yml logs -f
echo    Stop all:  docker-compose -f docker-compose-simple.yml down
echo.
pause
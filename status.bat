@echo off
echo.
echo 📊 IoT Backend - Service Status
echo ===============================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker is not running.
    pause
    exit /b 1
)

echo 📋 Service Status:
echo.
docker-compose ps

echo.
echo 🔍 Quick Health Checks:
echo.

REM Check if services are responding
echo Testing PostgreSQL connection...
docker exec pg-iot pg_isready -U iotuser -d iotdb >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ PostgreSQL: Ready
) else (
    echo ❌ PostgreSQL: Not ready
)

echo Testing Spring Boot API...
curl -s http://localhost:8080/api/devices >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Spring Boot API: Ready
) else (
    echo ❌ Spring Boot API: Not ready
)

echo.
echo 📜 To view logs:
echo    docker-compose logs -f [service-name]
echo.
echo Available services: postgres, mosquitto, iot-backend
echo.
pause
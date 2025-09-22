@echo off
echo.
echo 🛑 IoT Backend - Stopping All Services
echo =======================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker is not running. Nothing to stop.
    pause
    exit /b 1
)

echo 🛑 Stopping all IoT Backend services...
docker-compose down

echo.
echo ✅ All services stopped successfully!
echo.
echo 🔧 Additional cleanup options:
echo.
echo    ❓ Remove all data (database, logs): 
echo       stop-and-clean.bat
echo.
echo    📜 View service status:
echo       docker-compose ps
echo.
pause
@echo off
echo.
echo 🧹 IoT Backend - Complete Cleanup
echo ==================================
echo.
echo ⚠️  WARNING: This will remove ALL data including:
echo     - Database data
echo     - MQTT logs and data
echo     - Docker volumes
echo.
set /p confirm=Are you sure? (y/N): 
if /i not "%confirm%"=="y" (
    echo ❌ Cleanup cancelled.
    pause
    exit /b 0
)

echo.
echo 🛑 Stopping all services...
docker-compose down

echo.
echo 🧹 Removing all volumes and data...
docker-compose down -v

echo.
echo 🗑️  Removing orphaned containers...
docker-compose down --remove-orphans

echo.
echo ✅ Complete cleanup finished!
echo.
echo 💡 To start fresh:
echo    start.bat
echo.
pause
@echo off
echo.
echo 🔍 IoT Backend - Debug và Fix Lỗi
echo ==================================
echo.

echo 🛑 Dừng tất cả containers hiện tại...
docker-compose down --remove-orphans

echo.
echo 🧹 Xóa volumes cũ (nếu có vấn đề)...
docker-compose down -v

echo.
echo 📋 Kiểm tra Docker system...
docker system df

echo.
echo 🚀 Khởi động lại với logs...
echo.

REM Start containers one by one để dễ debug
echo 📦 Starting PostgreSQL first...
docker-compose up postgres -d

echo ⏳ Waiting 30 seconds for PostgreSQL...
timeout /t 30 /nobreak >nul

echo 📊 PostgreSQL status:
docker-compose ps postgres
docker-compose logs --tail=20 postgres

echo.
echo 📡 Starting Mosquitto...
docker-compose up mosquitto -d

echo ⏳ Waiting 30 seconds for Mosquitto...
timeout /t 30 /nobreak >nul

echo 📊 Mosquitto status:
docker-compose ps mosquitto  
docker-compose logs --tail=20 mosquitto

echo.
echo 🌱 Starting Spring Boot app...
docker-compose up iot-backend -d

echo ⏳ Waiting 60 seconds for Spring Boot...
timeout /t 60 /nobreak >nul

echo.
echo 📋 Final status của tất cả services:
docker-compose ps

echo.
echo 📜 Nếu vẫn có lỗi, xem logs chi tiết:
echo    docker-compose logs postgres
echo    docker-compose logs mosquitto
echo    docker-compose logs iot-backend
echo.
pause
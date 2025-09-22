@echo off
echo.
echo 🧪 IoT Backend - API Test Suite
echo ===============================
echo.

REM Check if the API is running
echo ✅ Kiểm tra API có đang chạy...
curl -s http://localhost:8080/api/devices >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ API không phản hồi tại http://localhost:8080
    echo    👉 Đảm bảo start.bat đã chạy thành công
    pause
    exit /b 1
)

echo ✅ API đang chạy!
echo.

echo 🧪 Chạy các test cơ bản...
echo.

REM Test 1: Get all devices
echo 📱 Test 1: Lấy danh sách devices
curl -s http://localhost:8080/api/devices | echo.
echo.

REM Test 2: Create a test device
echo 📝 Test 2: Tạo device mới
curl -X POST http://localhost:8080/api/devices ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Device\",\"type\":\"SENSOR\",\"location\":\"Lab 1\"}" ^
  -s | echo.
echo.

REM Test 3: Get devices again to see the new one
echo 📱 Test 3: Lấy danh sách devices (sau khi tạo mới)
curl -s http://localhost:8080/api/devices | echo.
echo.

REM Test 4: Check monitoring overview
echo 📊 Test 4: Kiểm tra monitoring overview
curl -s http://localhost:8080/api/monitoring/overview | echo.
echo.

REM Test 5: Get telemetry data
echo 📈 Test 5: Lấy telemetry data
curl -s http://localhost:8080/api/telemetry | echo.
echo.

echo ✅ Hoàn thành tất cả tests!
echo.
echo 💡 Các endpoint khác để test:
echo    GET  http://localhost:8080/api/devices
echo    POST http://localhost:8080/api/devices
echo    GET  http://localhost:8080/api/monitoring/overview  
echo    GET  http://localhost:8080/api/telemetry
echo.
echo 📡 Test MQTT (manual):
echo    docker exec mosquitto-iot mosquitto_pub -h localhost -t "iot/demo/temp" -m '{"temperature":25.5}'
echo    docker exec mosquitto-iot mosquitto_sub -h localhost -t "iot/demo/temp"
echo.
pause
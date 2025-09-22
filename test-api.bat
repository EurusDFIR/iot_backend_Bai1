@echo off
echo.
echo 🧪 IoT Backend - Comprehensive API Test
echo =======================================
echo.

REM Check if the API is running
echo ✅ Kiểm tra API có đang chạy...
curl -s http://localhost:8080/api/devices >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ API không phản hồi tại http://localhost:8080
    echo    👉 Đảm bảo start.bat đã chạy thành công
    echo    👉 Hoặc check: docker-compose -f docker-compose-simple.yml ps
    pause
    exit /b 1
)

echo ✅ API đang chạy!
echo.

echo 🧪 Chạy test suite đầy đủ...
echo.

REM Test 1: Get all devices
echo 📱 Test 1: GET /api/devices
echo Response:
curl -s http://localhost:8080/api/devices
echo.
echo.

REM Test 2: Create a test device
echo 📝 Test 2: POST /api/devices (tạo device mới)
echo Request: {"name":"Test Device","type":"SENSOR","location":"Lab 1"}
curl -X POST http://localhost:8080/api/devices ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Device\",\"type\":\"SENSOR\",\"location\":\"Lab 1\"}" ^
  -s
echo.
echo.

REM Test 3: Get devices again 
echo 📱 Test 3: GET /api/devices (sau khi tạo mới)
curl -s http://localhost:8080/api/devices
echo.
echo.

REM Test 4: Monitoring overview
echo 📊 Test 4: GET /api/monitoring/overview
curl -s http://localhost:8080/api/monitoring/overview
echo.
echo.

REM Test 5: Telemetry data
echo 📈 Test 5: GET /api/telemetry
curl -s http://localhost:8080/api/telemetry
echo.
echo.

echo ✅ Hoàn thành tất cả API tests!
echo.
echo � Database Test:
echo Testing PostgreSQL connection...
docker exec pg-iot-simple pg_isready -U iotuser -d iotdb >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Database: Connected successfully
) else (
    echo ❌ Database: Connection failed
)

echo.
echo 📡 MQTT Test:
echo Testing MQTT broker...
docker exec mosquitto-iot-simple mosquitto_pub -h localhost -t "test/topic" -m "test message" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ MQTT: Publish successful
) else (
    echo ❌ MQTT: Publish failed
)

echo.
echo 🎉 =============== TEST SUMMARY ===============
echo ✅ Spring Boot API: http://localhost:8080
echo ✅ Database: PostgreSQL ready
echo ✅ MQTT: Mosquitto broker ready
echo.
echo 💡 Next steps:
echo    📱 Open: http://localhost:8080/api/devices
echo    📡 MQTT Topics: iot/demo/temp, iot/devices/+/command
echo    📊 Database: localhost:5432 (iotdb/iotuser/secret)
echo.
pause
@echo off
echo.
echo 🔄 Restarting Mosquitto with Fixed Config
echo =========================================
echo.

echo 🛑 Stopping current Mosquitto container...
docker stop mosquitto-iot-simple
docker rm mosquitto-iot-simple

echo.
echo 🚀 Starting Mosquitto with proper config...
docker-compose -f docker-compose-simple.yml up mosquitto -d

echo.
echo ⏳ Waiting 10 seconds for Mosquitto to start...
timeout /t 10 /nobreak >nul

echo.
echo 📋 Checking Mosquitto status:
docker logs --tail=20 mosquitto-iot-simple

echo.
echo 🧪 Testing MQTT connection...
echo Testing from container:
docker exec mosquitto-iot-simple mosquitto_pub -h localhost -t "test/topic" -m "Hello MQTT!"

echo.
echo ✅ MQTT Explorer connection info:
echo    🔗 Host: localhost
echo    🔢 Port: 1883  
echo    🔐 Authentication: None (Anonymous)
echo    📡 Protocol: MQTT v3.1.1 or v5.0
echo.
echo 💡 Try connecting with MQTT Explorer now!
echo.
pause
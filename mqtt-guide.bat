@echo off
chcp 65001 >nul
echo.
echo 📡 MQTT Explorer Setup Guide
echo ===========================
echo.

echo 🔍 Checking MQTT Broker Status...
docker ps | findstr mosquitto >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Mosquitto container not running!
    echo    Run: start.bat first
    pause
    exit /b 1
)

echo ✅ Mosquitto container is running
echo.

echo 📋 Container Information:
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}" | findstr mosquitto

echo.
echo 🧪 Testing MQTT Connection...
docker exec mosquitto-iot-simple mosquitto_pub -h localhost -t "test/guide" -m "Connection test from guide" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ MQTT publish test successful!
) else (
    echo ❌ MQTT publish test failed!
    echo    Check container logs: docker logs mosquitto-iot-simple
)

echo.
echo 📱 MQTT Explorer Configuration:
echo ================================
echo.
echo 🎯 TCP Connection (Recommended):
echo    Name: IoT Backend  
echo    Protocol: mqtt://
echo    Host: localhost
echo    Port: 1883
echo    Username: (leave empty)
echo    Password: (leave empty)
echo    Clean Session: ✓
echo.
echo 🌐 WebSocket Connection (Alternative):
echo    Name: IoT Backend WS
echo    Protocol: ws://
echo    Host: localhost  
echo    Port: 9001
echo    Path: / (or leave empty)
echo    Username: (leave empty)
echo    Password: (leave empty)
echo.
echo 🧪 Test Topics after connecting:
echo    Subscribe: iot/demo/temp
echo    Subscribe: iot/device/+/command
echo    Publish to: iot/demo/temp
echo    Message: {"temperature":25.5,"humidity":60}
echo.
echo 💡 If still getting "Invalid URL" error:
echo    1. Make sure to select mqtt:// NOT ws:// for TCP
echo    2. Don't include any special characters in host field
echo    3. Try reconnecting after 30 seconds
echo    4. Check Windows Firewall isn't blocking ports
echo.
pause
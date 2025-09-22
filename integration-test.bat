@echo off
echo.
echo 🧪 Full Integration Test - Clone to Working
echo ==========================================
echo.
echo This script simulates what a new user would experience
echo when cloning the repository and running start.bat
echo.

echo 📋 Test Checklist:
echo [ ] Docker Desktop running
echo [ ] No existing containers conflicts  
echo [ ] All ports available
echo [ ] start.bat execution
echo [ ] API responding
echo [ ] MQTT broker working
echo [ ] MQTT Explorer can connect
echo.

echo 🔍 Step 1: Environment Check
echo =============================
call check-system.bat

echo.
echo 🛑 Step 2: Clean Environment
echo ============================
docker-compose -f docker-compose-simple.yml down --remove-orphans -v >nul 2>&1
docker system prune -f >nul 2>&1

echo.  
echo 🚀 Step 3: Fresh Start
echo ======================
call start.bat

echo.
echo 📡 Step 4: MQTT Test
echo ===================
call mqtt-guide.bat

echo.
echo ✅ Integration Test Complete!
echo ============================
echo.
echo If all tests passed, the project is ready for others to clone and run!
echo.
pause
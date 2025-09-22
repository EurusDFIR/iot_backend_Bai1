@echo off@echo off

echo.echo 🚀 IoT Backend - Docker All-in-One Setup

echo 🚀 IoT Backend - Starting Applicationecho =========================================

echo =====================================

echo.REM Check Docker

docker --version >nul 2>&1

REM Check Dockerif %ERRORLEVEL% NEQ 0 (

docker --version >nul 2>&1    echo ❌ Docker not found. Please install Docker Desktop first.

if %ERRORLEVEL% NEQ 0 (    echo 🔗 Download: https://www.docker.com/products/docker-desktop

    echo ❌ Docker not found. Please install Docker Desktop first.    pause

    echo 🔗 Download: https://www.docker.com/products/docker-desktop    exit /b 1

    echo.)

    pause

    exit /b 1docker info >nul 2>&1

)if %ERRORLEVEL% NEQ 0 (

    echo ❌ Docker not running. Please start Docker Desktop.

echo ✅ Docker found    pause

echo.    exit /b 1

)

REM Clean up any existing containers

echo 🧹 Cleaning up existing containers...echo ✅ Docker is ready

docker-compose down -v >nul 2>&1

REM Stop any existing containers

REM Start all servicesecho 🛑 Stopping existing containers...

echo 📦 Starting all services (database, MQTT, Spring Boot app)...docker-compose down -v

echo ⏳ This may take 2-3 minutes for first build...

echo.REM Build and start all services

docker-compose up --build -decho 🏗️  Building and starting all services...

echo    📦 PostgreSQL Database

REM Wait for servicesecho    📡 Mosquitto MQTT Broker

echo ⏳ Waiting 60 seconds for all services to be ready...echo    🌱 Spring Boot IoT Backend

timeout /t 60 /nobreak >nulecho.

echo ⏳ This may take 2-3 minutes for first build...

REM Check status

echo.docker-compose up --build -d

echo 📋 Service Status:

docker-compose psREM Wait for services to be healthy

echo ⏳ Waiting for all services to be ready...

REM Test API

echo.set max_attempts=30

echo 🧪 Testing API connection...set attempt=0

curl -s http://localhost:8080/api/devices >nul 2>&1

if %ERRORLEVEL% EQU 0 (:wait_loop

    echo ✅ SUCCESS! IoT Backend is running!docker-compose ps | findstr "iot-backend-app" | findstr "Up" >nul 2>&1

    echo.if %ERRORLEVEL% EQU 0 (

    echo 🌐 Available Services:    echo ✅ All services are running and healthy!

    echo    📱 Web API: http://localhost:8080    goto success

    echo    📊 Database: localhost:5432 (iotdb/iotuser/secret))

    echo    📡 MQTT: localhost:1883

    echo.set /a attempt+=1

    echo 🧪 Quick Tests:if %attempt% GEQ %max_attempts% (

    echo    curl http://localhost:8080/api/devices    echo ❌ Services took too long to start. Check logs:

    echo    curl http://localhost:8080/api/monitoring/overview    echo    docker-compose logs iot-backend

    echo.    pause

    echo 🔧 Useful Commands:    exit /b 1

    echo    docker-compose logs -f    (view logs))

    echo    docker-compose down       (stop all)

    echo    docker-compose restart    (restart all)echo    Still waiting... (%attempt%/%max_attempts%)

) else (timeout /t 10 /nobreak >nul

    echo ⚠️ API not ready yet. Services may still be starting...goto wait_loop

    echo 📜 Check logs with: docker-compose logs iot-backend

    echo ⏰ Wait a bit more and try: curl http://localhost:8080/api/devices:success

)echo.

echo 🎉 SUCCESS! IoT Backend is running!

echo.echo ==================================

pauseecho 📱 Web API: http://localhost:8080
echo 📊 Database: localhost:5432 (iotdb/iotuser/secret)
echo 📡 MQTT: localhost:1883
echo.
echo 🧪 Quick test:
echo curl http://localhost:8080/api/devices
echo.
echo 🔧 Useful commands:
echo    View logs:    docker-compose logs -f
echo    Stop all:     docker-compose down
echo    Restart:      docker-compose restart
echo    Clean reset:  docker-compose down -v ^&^& docker-compose up -d
echo.
pause
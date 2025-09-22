@echo off
echo 🚀 IoT Backend Setup for Windows...

REM Check Docker
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker not found. Please install Docker Desktop.
    pause
    exit /b 1
)

REM Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java not found. Please install JDK 17+.
    pause
    exit /b 1
)

echo ✅ Docker and Java found.

REM Start Docker services
echo 🐳 Starting Docker services...
docker-compose up -d

REM Wait for PostgreSQL to be ready
echo ⏳ Waiting for PostgreSQL to start (may take 30-60 seconds)...
:wait_postgres
docker-compose exec -T postgres psql -U iotuser -d iotdb -c "SELECT 1;" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Still waiting for PostgreSQL...
    timeout /t 5 /nobreak >nul
    goto wait_postgres
)

echo ✅ PostgreSQL is ready!

REM Try to run Spring Boot app
echo 🌱 Starting Spring Boot application...

REM Try Maven first
mvn --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using system Maven...
    mvn spring-boot:run
) else (
    echo Using Maven Wrapper...
    mvnw.cmd spring-boot:run
)

pause
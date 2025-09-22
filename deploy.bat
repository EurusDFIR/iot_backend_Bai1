@echo off
REM ==========================================
REM Production Deployment Script for Windows
REM ==========================================

setlocal enabledelayedexpansion

set COMPOSE_FILE=docker-compose.production.yml
set PROJECT_NAME=iot-backend-prod
set BACKUP_DIR=.\backups

REM Colors (limited support in CMD)
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

goto :main

:print_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:print_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:print_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:print_error
echo %RED%[ERROR]%NC% %~1
goto :eof

:check_requirements
call :print_info "Checking requirements..."

docker --version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Docker is not installed or not in PATH"
    exit /b 1
)

docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Docker Compose is not installed or not in PATH"
    exit /b 1
)

if not exist "%COMPOSE_FILE%" (
    call :print_error "Docker Compose file '%COMPOSE_FILE%' not found"
    exit /b 1
)

call :print_success "All requirements satisfied"
goto :eof

:backup_data
call :print_info "Creating backup..."

if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
)

for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set TIMESTAMP=%mydate%_%mytime::=%

set BACKUP_FILE=%BACKUP_DIR%\postgres_backup_%TIMESTAMP%.sql

docker ps | findstr "%PROJECT_NAME%_postgres" >nul 2>&1
if %errorlevel% equ 0 (
    docker exec %PROJECT_NAME%_postgres_1 pg_dump -U iotuser iotdb > "%BACKUP_FILE%"
    call :print_success "Database backup created: %BACKUP_FILE%"
) else (
    call :print_warning "PostgreSQL container not running, skipping backup"
)
goto :eof

:deploy
call :print_info "Starting production deployment..."

call :print_info "Pulling latest Docker images..."
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" pull

call :print_info "Building application..."
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" build --no-cache

call :print_info "Starting services..."
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" up -d

call :print_info "Waiting for services to be ready..."
timeout /t 30 /nobreak >nul

call :health_check
goto :eof

:health_check
call :print_info "Performing health check..."

docker exec %PROJECT_NAME%_postgres_1 pg_isready -U iotuser -d iotdb >nul 2>&1
if %errorlevel% equ 0 (
    call :print_success "PostgreSQL is healthy"
) else (
    call :print_error "PostgreSQL health check failed"
    call :show_logs postgres
    exit /b 1
)

docker exec %PROJECT_NAME%_mosquitto_1 mosquitto_pub -h localhost -t test -m "health_check" >nul 2>&1
if %errorlevel% equ 0 (
    call :print_success "Mosquitto is healthy"
) else (
    call :print_warning "Mosquitto health check failed (may be normal)"
)

timeout /t 20 /nobreak >nul
curl -f http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    call :print_success "Spring Boot application is healthy"
) else (
    call :print_error "Spring Boot application health check failed"
    call :show_logs app
    exit /b 1
)
goto :eof

:show_logs
set service=%1
call :print_info "Showing logs for %service%..."
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" logs --tail=50 "%service%"
goto :eof

:stop_services
call :print_info "Stopping services..."
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" down
call :print_success "Services stopped"
goto :eof

:cleanup
call :print_info "Cleaning up unused Docker resources..."
docker system prune -f
docker volume prune -f
call :print_success "Cleanup completed"
goto :eof

:show_status
call :print_info "Service status:"
docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" ps

call :print_info ""
call :print_info "Service URLs:"
echo   - Spring Boot API: http://localhost:8080
echo   - Health Check: http://localhost:8080/actuator/health
echo   - MQTT Broker: mqtt://localhost:1883
echo   - MQTT WebSocket: ws://localhost:9001
echo   - PostgreSQL: localhost:5432
goto :eof

:main
set command=%1
if "%command%"=="" set command=deploy

if "%command%"=="deploy" (
    call :check_requirements
    call :backup_data
    call :deploy
    call :show_status
) else if "%command%"=="backup" (
    call :check_requirements
    call :backup_data
) else if "%command%"=="health" (
    call :check_requirements
    call :health_check
) else if "%command%"=="logs" (
    if not "%2"=="" (
        call :show_logs %2
    ) else (
        docker-compose -f "%COMPOSE_FILE%" -p "%PROJECT_NAME%" logs -f
    )
) else if "%command%"=="stop" (
    call :stop_services
) else if "%command%"=="restart" (
    call :stop_services
    call :deploy
    call :show_status
) else if "%command%"=="status" (
    call :show_status
) else if "%command%"=="cleanup" (
    call :cleanup
) else if "%command%"=="help" (
    echo Usage: %0 [command]
    echo.
    echo Commands:
    echo   deploy    - Deploy the application ^(default^)
    echo   backup    - Create database backup
    echo   health    - Check service health
    echo   logs      - Show service logs
    echo   stop      - Stop all services
    echo   restart   - Restart all services
    echo   status    - Show service status
    echo   cleanup   - Clean up unused Docker resources
    echo   help      - Show this help message
) else (
    call :print_error "Unknown command: %command%"
    call :print_info "Use '%0 help' for available commands"
    exit /b 1
)

endlocal
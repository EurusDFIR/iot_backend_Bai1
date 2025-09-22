@echo off
echo.
echo 🔍 IoT Backend - System Check
echo =============================
echo.
echo Kiểm tra môi trường Windows trước khi chạy project...
echo.

set "error_count=0"

REM Check Docker Desktop
echo ✅ Kiểm tra Docker Desktop...
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker Desktop chưa được cài đặt
    echo    👉 Tải về: https://www.docker.com/products/docker-desktop
    set /a error_count+=1
) else (
    echo ✅ Docker Desktop đã cài đặt
)

REM Check if Docker is running
echo ✅ Kiểm tra Docker có đang chạy...
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker Desktop chưa được khởi động
    echo    👉 Mở Docker Desktop và đợi icon màu xanh
    set /a error_count+=1
) else (
    echo ✅ Docker Desktop đang chạy
)

REM Check Docker Compose
echo ✅ Kiểm tra Docker Compose...
docker-compose --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker Compose không khả dụng
    set /a error_count+=1
) else (
    echo ✅ Docker Compose khả dụng
)

REM Check if ports are available
echo ✅ Kiểm tra ports có sẵn...

netstat -ano | findstr :8080 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  Port 8080 đang được sử dụng (Spring Boot)
    echo    👉 Có thể cần dừng ứng dụng khác hoặc sử dụng port khác
    set /a error_count+=1
) else (
    echo ✅ Port 8080 sẵn sàng
)

netstat -ano | findstr :5432 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  Port 5432 đang được sử dụng (PostgreSQL)
    echo    👉 Có thể là PostgreSQL khác hoặc container cũ
) else (
    echo ✅ Port 5432 sẵn sàng
)

netstat -ano | findstr :1883 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  Port 1883 đang được sử dụng (MQTT)
    echo    👉 Có thể là MQTT broker khác hoặc container cũ
) else (
    echo ✅ Port 1883 sẵn sàng
)

REM Check available disk space (simplified check)
echo ✅ Kiểm tra dung lượng ổ đĩa...
for /f "tokens=3" %%a in ('dir /-c %SystemDrive%\ ^| find "bytes free"') do (
    if %%a LSS 2000000000 (
        echo ⚠️  Dung lượng ổ đĩa thấp
        echo    👉 Nên có ít nhất 2GB dung lượng trống
    ) else (
        echo ✅ Đủ dung lượng ổ đĩa
    )
)

REM Check if project files exist
echo ✅ Kiểm tra files dự án...
if not exist "docker-compose.yml" (
    echo ❌ Không tìm thấy docker-compose.yml
    echo    👉 Đảm bảo bạn đang ở thư mục gốc của project
    set /a error_count+=1
) else (
    echo ✅ docker-compose.yml tồn tại
)

if not exist "Dockerfile" (
    echo ❌ Không tìm thấy Dockerfile
    set /a error_count+=1
) else (
    echo ✅ Dockerfile tồn tại
)

if not exist "pom.xml" (
    echo ❌ Không tìm thấy pom.xml
    set /a error_count+=1
) else (
    echo ✅ pom.xml tồn tại
)

REM Summary
echo.
echo 📋 =============== KẾT QUẢ ===============
if %error_count% EQU 0 (
    echo.
    echo 🎉 ✅ HỆ THỐNG SẴN SÀNG!
    echo.
    echo 👉 Bạn có thể chạy: start.bat
    echo.
) else (
    echo.
    echo ❌ CÓ %error_count% VẤN ĐỀ CẦN KHẮC PHỤC
    echo.
    echo 💡 Khắc phục các vấn đề trên rồi chạy lại check-system.bat
    echo.
)

echo 🔧 Các lệnh hữu ích:
echo    start.bat           - Khởi động toàn bộ hệ thống  
echo    stop.bat            - Dừng tất cả services
echo    status.bat          - Kiểm tra trạng thái
echo    stop-and-clean.bat  - Dọn sạch và reset
echo.
pause
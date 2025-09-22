@echo off
echo.
echo 🔧 Port 8080 Cleanup Script
echo ===========================
echo.

echo Tìm process đang sử dụng port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Found PID: %%a
    echo Killing process %%a...
    taskkill /PID %%a /F >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo ✅ Successfully killed process %%a
    ) else (
        echo ⚠️  Could not kill process %%a ^(may already be dead^)
    )
)

echo.
echo ✅ Port 8080 cleanup completed!
echo.
pause
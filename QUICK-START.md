# 🚀 QUICK START GUIDE - 2 PHÚT SETUP

## Bước 1: Cài Docker Desktop

- Tải về: https://www.docker.com/products/docker-desktop
- Cài đặt và khởi động (đợi icon màu xanh)

## Bước 2: Clone và chạy

```cmd
git clone <repository-url>
cd iot-backend
start.bat
```

> **💡 Script tự động:** Kill port 8080, dọn containers cũ, build & start!

## Bước 3: Test (sau 2-3 phút)

- **API**: http://localhost:8080/api/devices
- **Database**: localhost:5432 (iotdb/iotuser/secret)
- **MQTT TCP**: localhost:1883 (for MQTT Explorer)
- **MQTT WebSocket**: localhost:9001 (alternative)

### 📡 MQTT Explorer Setup:

```
Protocol: mqtt://
Host: localhost
Port: 1883
Username: (empty)
Password: (empty)
```

## Scripts hữu ích:

- `start.bat` - **Main start script** (auto-fix all issues)
- `mqtt-guide.bat` - **MQTT Explorer setup guide**
- `test-api.bat` - Test toàn diện APIs
- `kill-port-8080.bat` - Fix port conflicts
- `debug-start.bat` - Debug step-by-step

## Troubleshooting:

- **Port conflict:** Script tự động fix
- **Container conflicts:** `docker-compose -f docker-compose-simple.yml down --remove-orphans`
- **MQTT Explorer errors:** Run `mqtt-guide.bat` for detailed setup
- **Logs:** `docker-compose -f docker-compose-simple.yml logs iot-backend`

## ✅ Đã test thực tế:

- ✅ Container conflicts: Auto-fixed
- ✅ Port 8080 conflicts: Auto-cleaned
- ✅ Health checks: Removed (causes issues)
- ✅ File mounting: Simplified for Windows

**🎉 Clone → start.bat → Enjoy! (Really works!)**

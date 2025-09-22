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

## Bước 3: Test

- API: http://localhost:8080/api/devices
- Database: localhost:5432 (iotdb/iotuser/secret)
- MQTT: localhost:1883

## Scripts hữu ích:

- `check-system.bat` - Kiểm tra hệ thống
- `start.bat` - Khởi động
- `stop.bat` - Dừng
- `status.bat` - Kiểm tra trạng thái
- `test-api.bat` - Test API

## Troubleshooting:

- Lỗi port: Xem README.md phần troubleshooting
- Logs: `docker-compose logs iot-backend`
- Reset: `stop-and-clean.bat` + `start.bat`

**🎉 Done! Happy coding!**

# 🚀 IoT Backend - Spring Boot Application

## ⚡ Quick Start cho Windows (2 phút setup)

### 🎯 **Siêu Đơn Giản - Chỉ cần Docker!**

**Yêu cầu duy nhất: Docker Desktop for Windows**

#### 📋 **Bước 1: Cài Docker Desktop (nếu chưa có)**

1. Tải về: https://www.docker.com/products/docker-desktop
2. Cài đặt và khởi động Docker Desktop
3. Đợi Docker sẵn sàng (icon Docker ở system tray màu xanh)

#### 🚀 **Bước 2: Clone và chạy**

```cmd
# Clone repository
git clone <repository-url>
cd iot-backend

# WINDOWS: Double-click start.bat hoặc:
start.bat
```

> **💡 Lưu ý:** Script sẽ tự động:
>
> - Kiểm tra Docker Desktop
> - Dọn sạch port 8080 nếu bị chiếm
> - Khởi động tất cả services
> - Test API để đảm bảo hoạt động

#### ✅ **XONG! Trong 2-3 phút:**

- **🌱 Spring Boot App:** http://localhost:8080
- **📊 PostgreSQL:** localhost:5432 (iotdb/iotuser/secret)
- **📡 MQTT Broker:** localhost:1883
- **🌐 MQTT WebSocket:** localhost:9001#### 🧪 **Bước 3: Test thử**

```cmd
# Test API endpoints
curl http://localhost:8080/api/devices
curl http://localhost:8080/api/monitoring/overview

# Hoặc mở browser:
# http://localhost:8080/api/devices
```

## 🔧 **Quản lý dự án (Windows Scripts)**

Project đã được tối ưu với các script Windows để dễ sử dụng:

```cmd
start.bat           # 🚀 Khởi động toàn bộ hệ thống
stop.bat            # 🛑 Dừng tất cả services
stop-and-clean.bat  # 🧹 Dừng và xóa hết data (clean reset)
status.bat          # 📊 Kiểm tra trạng thái services
```

## 🆘 **Troubleshooting cho Windows**

### ❌ Docker Desktop không khởi động được:

```cmd
# 1. Kiểm tra Hyper-V đã bật chưa (PowerShell as Admin):
Enable-WindowsOptionalFeature -Online -FeatureName VirtualMachinePlatform

# 2. Khởi động lại Windows
# 3. Mở Docker Desktop và đợi icon màu xanh
```

### ❌ Services không start được:

```cmd
# Xem logs chi tiết:
docker-compose logs iot-backend
docker-compose logs postgres
docker-compose logs mosquitto

# Hard reset (mất data):
stop-and-clean.bat
start.bat
```

### ❌ Port đã được sử dụng:

```cmd
# Kiểm tra port nào đang bị chiếm:
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :1883

# Kill process nếu cần:
taskkill /PID <PID_NUMBER> /F
```

---

## 📝 **Mô tả kỹ thuật**

Ứng dụng Spring Boot backend cho hệ thống IoT, bao gồm **7 Labs đầy đủ**:

- **Lab 1:** Spring Boot Backend + Database Setup
- **Lab 2:** MQTT Client Integration
- **Lab 3:** Device CRUD API
- **Lab 4:** Telemetry Save to Database
- **Lab 5:** Device Command API
- **Lab 6:** Dynamic Subscribe & Monitoring
- **Lab 7:** Data Storage Optimization

## 🏗️ **Kiến trúc hệ thống**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │    │   PostgreSQL    │    │   Mosquitto     │
│   (Port 8080)   │◄──►│   (Port 5432)   │    │   MQTT Broker   │
│                 │    │                 │    │   (Port 1883)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
          ▲                                              ▲
          │                                              │
          ▼                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Docker Network                              │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 **Chi tiết cấu hình**

### Database (PostgreSQL)

- **Database:** `iotdb`
- **Username:** `iotuser`
- **Password:** `secret`
- **Port:** `5432`
- **Auto-Init:** Có (từ `init-db.sql`)

### MQTT Broker (Mosquitto)

- **Port:** `1883` (MQTT)
- **WebSocket:** `9001`
- **Config:** `mosquitto.conf`
- **Anonymous:** Allowed

### Application Properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/iotdb
spring.datasource.username=iotuser
spring.datasource.password=secret

# MQTT
mqtt.host=localhost
mqtt.port=1883
mqtt.clientId=iot-backend-${random.uuid}
```

## 🌐 **API Endpoints**

App sẽ chạy trên: **http://localhost:8080**

### Lab 3: Device CRUD API

```
GET    /api/devices              # Lấy danh sách devices
POST   /api/devices              # Tạo device mới
GET    /api/devices/{id}         # Lấy device theo ID
PUT    /api/devices/{id}         # Cập nhật device
DELETE /api/devices/{id}         # Xóa device
GET    /api/devices/{id}/telemetry  # Lấy telemetry của device
```

### Lab 4: Telemetry API

```
GET    /api/telemetry            # Lấy tất cả telemetry data
POST   /api/telemetry            # Lưu telemetry data mới
GET    /api/telemetry/device/{deviceId}  # Lấy telemetry theo device
```

### Lab 5: Device Commands

```
POST   /api/devices/{id}/command # Gửi command tới device
GET    /api/commands             # Lấy tất cả commands
GET    /api/commands/{id}        # Lấy command theo ID
```

### Lab 6: Monitoring & Dynamic Subscribe

```
GET    /api/monitoring/overview  # Tổng quan hệ thống
GET    /api/monitoring/devices   # Trạng thái devices
POST   /api/monitoring/subscribe/{deviceId}   # Subscribe device
DELETE /api/monitoring/unsubscribe/{deviceId} # Unsubscribe device
```

### Lab 7: Data Storage Optimization

```
POST   /api/data/archive         # Archive old telemetry data
GET    /api/data/archive/status  # Kiểm tra trạng thái archive
DELETE /api/data/cleanup         # Cleanup old archived data
```

## 🧪 **Testing & MQTT**

### Test MQTT với Command Line

```bash
# Subscribe to telemetry topic
docker exec mosquitto-iot mosquitto_sub -h localhost -t "iot/demo/temp"

# Publish test telemetry data
docker exec mosquitto-iot mosquitto_pub -h localhost -t "iot/demo/temp" -m '{"deviceId":"test-01","temperature":25.5,"humidity":60,"timestamp":"2024-01-01T10:00:00Z"}'

# Subscribe to command topics
docker exec mosquitto-iot mosquitto_sub -h localhost -t "iot/devices/+/command"
```

### Test với Postman hoặc curl

```bash
# Tạo device mới
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Device","type":"SENSOR","location":"Lab 1"}'

# Lấy danh sách devices
curl http://localhost:8080/api/devices

# Gửi command
curl -X POST http://localhost:8080/api/devices/1/command \
  -H "Content-Type: application/json" \
  -d '{"command":"SET_TEMP","value":"25"}'
```

## 🎯 **Development Mode (Không dùng Docker)**

Nếu muốn development và có môi trường Java sẵn:

### Yêu cầu:

- Java 17+
- Maven 3.6+
- PostgreSQL 12+ đã cài
- Mosquitto MQTT Broker đã cài

### Setup:

```bash
# 1. Start database và MQTT (Manual hoặc Docker)
docker-compose up postgres mosquitto -d

# 2. Chạy Spring Boot app
./mvnw spring-boot:run

# Hoặc với IDE: Run IotBackendApplication.java
```

## 📦 **Docker Compose Services**

File `docker-compose.yml` định nghĩa 3 services:

- **postgres:** PostgreSQL database với data persistent
- **mosquitto:** MQTT Broker với config từ `mosquitto.conf`
- **iot-backend:** Spring Boot app, build từ source code

Tất cả services có health checks và auto-restart.

## 📚 **Lab Documentation**

Trong thư mục project có đầy đủ tài liệu cho từng Lab:

- `Lab1_SpringBoot_Backend_Detailed.md`
- `Lab2_MQTT_Client_Integration_Detailed.md`
- `Lab3_Device_CRUD_API_Detailed.md`
- `Lab4_Telemetry_Save_DB_Detailed.md`
- `Lab5_Device_Command_API_Detailed.md`
- `Lab6_Dynamic_Subscribe_Monitoring_Detailed.md`
- `Lab7_Data_Storage_Optimization_Detailed.md`

## 🤝 **Contributing**

1. Fork the project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

## 📄 **License**

Distributed under the MIT License. See `LICENSE` for more information.

---

## 💡 **Tips & Best Practices**

### Performance:

- Database connection pool được tối ưu cho 20 connections
- MQTT client sử dụng connection pooling
- JPA batch processing enabled cho bulk operations

### Security:

- MQTT hiện tại allow anonymous (development mode)
- Database credentials trong environment variables
- Docker containers run với non-root user

### Monitoring:

- Health checks enabled cho tất cả services
- Metrics endpoint: `/actuator/health`
- MQTT connection status monitoring

---

**🎉 Happy coding! Nếu có vấn đề gì, check troubleshooting section trước nhé!**

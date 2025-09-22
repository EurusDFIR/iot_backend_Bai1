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

### 📋 **POSTMAN Collection - Chi tiết Test API**

Tải Postman về: https://www.postman.com/downloads/

#### **1️⃣ Setup Environment**

Tạo Environment mới trong Postman:

- **Name:** `IoT Backend Local`
- **Variables:**
  - `base_url`: `http://localhost:8080`
  - `device_id`: `1` (sẽ được update động)

#### **2️⃣ Collection: Lab 3 - Device CRUD API**

```json
{
  "info": {
    "name": "IoT Backend - Device API Tests",
    "description": "Complete test collection for IoT Backend"
  },
  "item": [
    {
      "name": "1. Get All Devices",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/devices"
      },
      "test": [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Response is array', function () {",
        "    pm.expect(pm.response.json()).to.be.an('array');",
        "});"
      ]
    },
    {
      "name": "2. Create New Device",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/devices",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"name\": \"Test Sensor {{$randomInt}}\",\n  \"type\": \"SENSOR\",\n  \"location\": \"Lab Room A\",\n  \"description\": \"Temperature and humidity sensor\"\n}"
        }
      },
      "test": [
        "pm.test('Status code is 201', function () {",
        "    pm.response.to.have.status(201);",
        "});",
        "pm.test('Device created with ID', function () {",
        "    const device = pm.response.json();",
        "    pm.expect(device).to.have.property('id');",
        "    pm.environment.set('device_id', device.id);",
        "});"
      ]
    },
    {
      "name": "3. Get Device by ID",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/devices/{{device_id}}"
      },
      "test": [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Device has required fields', function () {",
        "    const device = pm.response.json();",
        "    pm.expect(device).to.have.property('id');",
        "    pm.expect(device).to.have.property('name');",
        "    pm.expect(device).to.have.property('type');",
        "});"
      ]
    },
    {
      "name": "4. Update Device",
      "request": {
        "method": "PUT",
        "url": "{{base_url}}/api/devices/{{device_id}}",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"name\": \"Updated Test Sensor\",\n  \"type\": \"SENSOR\",\n  \"location\": \"Lab Room B\",\n  \"description\": \"Updated description\"\n}"
        }
      }
    }
  ]
}
```

#### **3️⃣ Collection: Lab 4 - Telemetry API**

```json
{
  "name": "Telemetry Tests",
  "item": [
    {
      "name": "1. Get All Telemetry",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/telemetry"
      }
    },
    {
      "name": "2. Create Telemetry Data",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/telemetry",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"deviceId\": {{device_id}},\n  \"temperature\": {{$randomFloat}},\n  \"humidity\": {{$randomInt}},\n  \"timestamp\": \"{{$isoTimestamp}}\"\n}"
        }
      }
    },
    {
      "name": "3. Get Telemetry by Device",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/telemetry/device/{{device_id}}"
      }
    }
  ]
}
```

#### **4️⃣ Collection: Lab 5 - Command API**

```json
{
  "name": "Command Tests",
  "item": [
    {
      "name": "1. Send Device Command",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/devices/{{device_id}}/command",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"command\": \"SET_TEMP\",\n  \"value\": \"25.5\",\n  \"description\": \"Set temperature threshold\"\n}"
        }
      }
    },
    {
      "name": "2. Get All Commands",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/commands"
      }
    },
    {
      "name": "3. Get Command by ID",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/commands/1"
      }
    }
  ]
}
```

#### **5️⃣ Collection: Lab 6 - Monitoring API**

```json
{
  "name": "Monitoring Tests",
  "item": [
    {
      "name": "1. System Overview",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/monitoring/overview"
      },
      "test": [
        "pm.test('Has system stats', function () {",
        "    const response = pm.response.json();",
        "    pm.expect(response).to.have.property('totalDevices');",
        "    pm.expect(response).to.have.property('onlineDevices');",
        "});"
      ]
    },
    {
      "name": "2. All Device Status",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/monitoring/devices"
      }
    },
    {
      "name": "3. Online Devices",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/monitoring/devices/online"
      }
    },
    {
      "name": "4. Mark Device Online",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/monitoring/devices/{{device_id}}/online"
      }
    },
    {
      "name": "5. Dashboard Data",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/monitoring/dashboard"
      }
    }
  ]
}
```

### **💡 Cách Import Collection vào Postman:**

1. **Mở Postman** → Click "Import"
2. **Copy-paste** JSON collection vào tab "Raw text"
3. **Click "Continue"** → "Import"
4. **Set Environment:** Chọn "IoT Backend Local" environment
5. **Run Collection:** Click "Run" để test tất cả APIs

### 📡 **MQTT Explorer - Chi tiết Test MQTT**

Tải MQTT Explorer: http://mqtt-explorer.com/

#### **🔧 Setup Connection**

**Connection Settings:**

```
Protocol: mqtt://
Host: localhost
Port: 1883
Username: (để trống)
Password: (để trống)
Client ID: mqtt-explorer-test
```

**Advanced Settings:**

- Keep Alive: 60
- Clean Session: ✅ Enabled
- Auto Reconnect: ✅ Enabled

#### **📊 Test Topics Structure**

Project sử dụng topic structure:

```
iot/
├── devices/
│   ├── {deviceId}/
│   │   ├── telemetry     # Device gửi dữ liệu sensor
│   │   ├── command       # Server gửi lệnh đến device
│   │   └── status        # Device báo trạng thái
│   └── broadcast/        # Lệnh broadcast tới all devices
└── system/
    ├── health            # Health check messages
    └── logs              # System logs
```

#### **📝 Test Scenarios**

**Scenario 1: Telemetry Data Testing**

1. **Subscribe topics:**

   - `iot/devices/+/telemetry`
   - `iot/devices/1/telemetry`

2. **Publish test data:**
   ```json
   Topic: iot/devices/1/telemetry
   QoS: 1
   Payload:
   {
     "deviceId": 1,
     "temperature": 24.5,
     "humidity": 65.2,
     "timestamp": "2024-01-15T10:30:00Z"
   }
   ```

**Scenario 2: Command Testing**

1. **Subscribe to command topics:**

   - `iot/devices/+/command`
   - `iot/devices/1/command`

2. **Gửi command qua Postman API**, quan sát message trong MQTT Explorer

3. **Manual command publish:**
   ```json
   Topic: iot/devices/1/command
   QoS: 1
   Payload:
   {
     "commandId": "cmd-123",
     "command": "SET_TEMP",
     "value": "26.0",
     "timestamp": "2024-01-15T10:35:00Z"
   }
   ```

**Scenario 3: Device Status Monitoring**

1. **Subscribe:**

   - `iot/devices/+/status`

2. **Publish device status:**
   ```json
   Topic: iot/devices/1/status
   Payload:
   {
     "deviceId": 1,
     "status": "ONLINE",
     "lastSeen": "2024-01-15T10:40:00Z",
     "uptime": 3600
   }
   ```

### Test MQTT với Command Line

```bash
# Subscribe to telemetry topic
docker exec mosquitto-iot-simple mosquitto_sub -h localhost -t "iot/devices/+/telemetry"

# Publish test telemetry data
docker exec mosquitto-iot-simple mosquitto_pub -h localhost -t "iot/devices/1/telemetry" -m '{"deviceId":1,"temperature":25.5,"humidity":60,"timestamp":"2024-01-01T10:00:00Z"}'

# Subscribe to command topics
docker exec mosquitto-iot-simple mosquitto_sub -h localhost -t "iot/devices/+/command"

# Test command publish
docker exec mosquitto-iot-simple mosquitto_pub -h localhost -t "iot/devices/1/command" -m '{"command":"SET_TEMP","value":"25"}'
```

### Test với curl

```bash
# Tạo device mới
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Device","type":"SENSOR","location":"Lab 1"}'

# Lấy danh sách devices
curl http://localhost:8080/api/devices

# Gửi command (xem kết quả trong MQTT Explorer)
curl -X POST http://localhost:8080/api/devices/1/command \
  -H "Content-Type: application/json" \
  -d '{"command":"SET_TEMP","value":"25"}'

# Kiểm tra monitoring
curl http://localhost:8080/api/monitoring/overview
```

## 🎯 **End-to-End Testing Scenarios**

### **🔄 Scenario 1: Device Lifecycle Testing**

**Mô tả:** Test toàn bộ lifecycle của một IoT device từ tạo → gửi data → nhận command → monitoring

**Steps:**

1. **Tạo device qua Postman:**

   ```http
   POST /api/devices
   {
     "name": "Smart Thermostat",
     "type": "SENSOR",
     "location": "Living Room"
   }
   ```

2. **Subscribe MQTT topics trong MQTT Explorer:**

   - `iot/devices/+/telemetry`
   - `iot/devices/+/command`
   - `iot/devices/+/status`

3. **Device gửi telemetry data (qua MQTT Explorer):**

   ```json
   Topic: iot/devices/1/telemetry
   {
     "deviceId": 1,
     "temperature": 22.5,
     "humidity": 45.0,
     "timestamp": "2024-01-15T10:00:00Z"
   }
   ```

4. **Kiểm tra data đã lưu vào DB (qua Postman):**

   ```http
   GET /api/telemetry/device/1
   ```

5. **Gửi command từ server (qua Postman):**

   ```http
   POST /api/devices/1/command
   {
     "command": "SET_TEMP",
     "value": "24.0"
   }
   ```

6. **Xác nhận command được gửi qua MQTT:**

   - Quan sát message xuất hiện trong MQTT Explorer topic `iot/devices/1/command`

7. **Device báo status online:**

   ```json
   Topic: iot/devices/1/status
   {
     "deviceId": 1,
     "status": "ONLINE",
     "lastSeen": "2024-01-15T10:05:00Z"
   }
   ```

8. **Kiểm tra monitoring dashboard:**
   ```http
   GET /api/monitoring/dashboard
   GET /api/monitoring/devices/1
   ```

### **🌐 Scenario 2: Multi-Device IoT Network**

**Setup 3 devices cùng lúc:**

1. **Tạo 3 devices khác nhau:**

   - Temperature Sensor (ID: 1)
   - Humidity Sensor (ID: 2)
   - Smart Light (ID: 3)

2. **Mỗi device gửi data khác nhau:**

   ```bash
   # Device 1 - Temperature
   iot/devices/1/telemetry: {"deviceId":1,"temperature":25.5}

   # Device 2 - Humidity
   iot/devices/2/telemetry: {"deviceId":2,"humidity":60.0}

   # Device 3 - Light status
   iot/devices/3/telemetry: {"deviceId":3,"brightness":75,"color":"warm"}
   ```

3. **Test broadcast command:**

   ```http
   POST /api/devices/broadcast/command
   {
     "command": "HEALTH_CHECK",
     "broadcast": true
   }
   ```

4. **Monitor system overview:**
   ```http
   GET /api/monitoring/overview
   # Expected: totalDevices: 3, onlineDevices: 3
   ```

### **⚡ Scenario 3: Real-time Performance Testing**

**Mô phỏng high-frequency data:**

1. **Setup continuous telemetry stream:**

   - Script Python gửi data mỗi 5 giây
   - Monitor MQTT Explorer để xem realtime
   - Check database growth

2. **Load testing script:**

   ```python
   import time
   import json
   import paho.mqtt.client as mqtt

   client = mqtt.Client()
   client.connect("localhost", 1883, 60)

   for i in range(100):
       data = {
           "deviceId": 1,
           "temperature": 20 + (i % 10),
           "timestamp": time.time()
       }
       client.publish("iot/devices/1/telemetry", json.dumps(data))
       time.sleep(1)
   ```

3. **Monitor performance:**
   ```http
   GET /api/monitoring/dashboard
   GET /api/telemetry/device/1?limit=50
   ```

### **🔍 Scenario 4: Error Handling & Edge Cases**

1. **Test invalid device ID:**

   ```http
   GET /api/devices/999
   # Expected: 404 Not Found
   ```

2. **Test malformed MQTT message:**

   ```
   Topic: iot/devices/1/telemetry
   Payload: "invalid json data"
   # Check logs for error handling
   ```

3. **Test device offline scenario:**

   ```http
   POST /api/monitoring/devices/1/offline
   GET /api/monitoring/devices/offline
   ```

4. **Test command to non-existent device:**
   ```http
   POST /api/devices/999/command
   {
     "command": "TEST"
   }
   # Expected: 400 Bad Request
   ```

## 📊 **Testing Checklist**

### ✅ **API Testing (Postman)**

- [ ] All CRUD operations work for devices
- [ ] Telemetry data is saved correctly
- [ ] Commands are sent and stored
- [ ] Monitoring endpoints return correct data
- [ ] Error handling works for invalid requests
- [ ] Response times are acceptable (<500ms)

### ✅ **MQTT Testing (MQTT Explorer)**

- [ ] Can connect to broker successfully
- [ ] Subscribe to wildcard topics works
- [ ] Published messages appear in correct topics
- [ ] QoS levels work as expected
- [ ] Retained messages work correctly
- [ ] Large payloads are handled properly

### ✅ **Integration Testing**

- [ ] API commands trigger MQTT messages
- [ ] MQTT telemetry appears in database
- [ ] Device status updates correctly
- [ ] Monitoring data reflects reality
- [ ] End-to-end workflows complete successfully

### ✅ **Performance Testing**

- [ ] System handles 100+ messages/minute
- [ ] Database queries remain fast
- [ ] Memory usage stays stable
- [ ] No connection leaks in MQTT
- [ ] Concurrent users supported

## 🐛 **Common Issues & Solutions**

| Issue                      | Symptom                        | Solution                                               |
| -------------------------- | ------------------------------ | ------------------------------------------------------ |
| **MQTT Connection Failed** | Can't connect in MQTT Explorer | Check Docker containers running, use `mqtt-guide.bat`  |
| **API 404 Errors**         | Endpoints not found            | Verify Spring Boot started completely, check port 8080 |
| **Empty Response**         | APIs return `[]`               | Check database initialized, run `integration-test.bat` |
| **MQTT Messages Missing**  | Published but not received     | Verify topic names, check container networking         |
| **Slow Performance**       | High response times            | Restart containers, check database connections         |

## 🎓 **Learning Objectives Achieved**

Sau khi hoàn thành testing:

- ✅ **Lab 1-2:** Spring Boot + MQTT integration working
- ✅ **Lab 3:** Complete Device CRUD API
- ✅ **Lab 4:** Telemetry data flow (MQTT → Database)
- ✅ **Lab 5:** Command system (API → MQTT → Device)
- ✅ **Lab 6:** Real-time monitoring & dynamic subscriptions
- ✅ **Lab 7:** Performance optimization & data management

**🎉 Project hoàn chính là một IoT backend platform đầy đủ tính năng!**

---

## 📚 **Documentation & Testing**

### **Tài liệu chi tiết:**

- `TESTING_GUIDE.md` - **Hướng dẫn test chi tiết với Postman & MQTT Explorer**
- `QUICK-START.md` - Setup nhanh 2 phút
- `Lab1-7_*.md` - Chi tiết từng Lab

### **Scripts hữu ích:**

- `start.bat` - **Main startup script**
- `mqtt-guide.bat` - Hướng dẫn MQTT Explorer setup
- `integration-test.bat` - Test toàn diện tự động
- `debug-start.bat` - Debug step-by-step

**💡 Khuyến nghị:** Đọc `TESTING_GUIDE.md` để test đầy đủ tính năng với Postman và MQTT Explorer!

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

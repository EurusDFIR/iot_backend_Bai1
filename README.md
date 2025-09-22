# 🚀 IoT Backend - Spring Boot Application

## ⚡ Quick Start (2 phút setup)

### 🎯 **Super Simple - Docker All-in-One (Khuyến nghị)**

**Chỉ cần Docker - không cần Java, Maven, PostgreSQL gì cả!**

```bash
# Clone repository
git clone <repository-url>
cd iot-backend

# Windows - double click hoặc:
start.bat

# Linux/Mac/Git Bash:
chmod +x start.sh
./start.sh
```

**✅ XONG! Tất cả chạy trong Docker:**

- **🌱 Spring Boot App:** http://localhost:8080
- **📊 PostgreSQL:** localhost:5432
- **📡 MQTT Broker:** localhost:1883

### 🎯 **Manual Docker (Alternative)**

````bash
# 1. Build và start tất cả
docker-compose up --build -d

# 2. Check status
docker-compose ps

# 3. Test API
curl http://localhost:8080/api/devices
```---

## 📝 Mô tả

Ứng dụng Spring Boot backend cho hệ thống IoT, bao gồm **7 Labs đầy đủ**:

- **Lab 1:** Spring Boot Backend + Database
- **Lab 2:** MQTT Client Integration
- **Lab 3:** Device CRUD API
- **Lab 4:** Telemetry Save to Database
- **Lab 5:** Device Command API
- **Lab 6:** Dynamic Subscribe & Monitoring
- **Lab 7:** Data Storage Optimization

## 🔧 Yêu cầu hệ thống

- **Java 17+** ⚠️ **QUAN TRỌNG:** Đảm bảo JAVA_HOME được set đúng
- **Maven 3.6+** (optional - có sẵn Maven Wrapper)
- **Docker & Docker Compose** (cho database và MQTT broker)
- ~~**PostgreSQL 12+**~~ (dùng Docker thay thế)
- ~~**Mosquitto MQTT Broker**~~ (dùng Docker thay thế)

## 🏗️ Setup Environment

### 1. Clone Repository

```bash
git clone <repository-url>
cd iot-backend
````

### 2. Setup với Docker (Khuyến nghị) 🐳

**Cách nhanh nhất - chỉ cần Docker:**

```bash
# Chạy PostgreSQL và Mosquitto với Docker Compose
docker-compose up -d

# Kiểm tra containers đang chạy
docker-compose ps

# Xem logs
docker-compose logs postgres
docker-compose logs mosquitto
```

**Sau khi containers chạy, database và MQTT broker đã sẵn sàng tại:**

- **PostgreSQL:** `localhost:5432` (Database: `iotdb`, User: `iotuser`, Pass: `secret`)
- **Mosquitto MQTT:** `localhost:1883`

### 3. Setup Manual (Alternative)

#### PostgreSQL Database

```sql
-- Tạo database và user
CREATE DATABASE iotdb;
CREATE USER iotuser WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE iotdb TO iotuser;

-- Connect vào database iotdb và grant schema permissions
\c iotdb
GRANT ALL ON SCHEMA public TO iotuser;
```

#### Mosquitto MQTT Broker

##### Windows:

```bash
# Download từ https://mosquitto.org/download/
# Hoặc dùng Chocolatey
choco install mosquitto

net start mosquitto
```

##### Linux/Mac:

```bash
# Ubuntu/Debian
sudo apt-get install mosquitto mosquitto-clients

# Start service
sudo systemctl start mosquitto
sudo systemctl enable mosquitto
```

### 4. Verify Setup

```bash
# Với Docker Compose (containers đang chạy)
docker-compose exec postgres psql -U iotuser -d iotdb -c "SELECT version();"
docker-compose exec mosquitto mosquitto_pub -h localhost -t test/topic -m "Hello MQTT"

# Với manual setup
# Test PostgreSQL connection
psql -h localhost -U iotuser -d iotdb

# Test MQTT broker
mosquitto_pub -h localhost -t test/topic -m "Hello MQTT"
mosquitto_sub -h localhost -t test/topic
```

## 🚀 Chạy ứng dụng

### Cách 1: Maven Wrapper (Recommended)

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Cách 2: Maven thông thường

```bash
mvn spring-boot:run
```

### Cách 3: Package và chạy JAR

```bash
mvn clean package
java -jar target/iot-backend-0.0.1-SNAPSHOT.jar
```

## 🌐 API Endpoints

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

### Lab 5: Device Commands

```
POST   /api/devices/{id}/command # Gửi command tới device
```

### Lab 6: Monitoring & Dynamic Subscribe

```
GET    /api/monitoring/overview  # Tổng quan hệ thống
GET    /api/monitoring/devices   # Danh sách device status
POST   /api/mqtt/subscribe       # Đăng ký topic MQTT
GET    /api/mqtt/recent          # Messages gần nhất
```

### Lab 7: Data Optimization

```
GET    /api/data-optimization/archive/statistics  # Thống kê archive
POST   /api/data-optimization/archive/force       # Force archive data
GET    /api/data-optimization/storage/usage       # Storage usage
GET    /api/data-optimization/recommendations     # Khuyến nghị tối ưu
```

## 📡 MQTT Topics

App tự động subscribe các topics:

- `iot/demo/temp` - Demo temperature data (Lab 2)
- `iot/device/+/telemetry` - Device telemetry data (Lab 4)
- `iot/device/+/heartbeat` - Device heartbeat (Lab 6)
- `iot/device/+/status` - Device status updates (Lab 6)

App publish commands tới:

- `iot/device/{id}/command` - Send commands to devices (Lab 5)

## 🧪 Testing

Xem file **`IoT_Labs_Testing_Guide.md`** để có hướng dẫn test chi tiết với Postman và MQTT Explorer.

### Quick Test với cURL

```bash
# Test Lab 3: Tạo device
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Test-Device","type":"sensor","status":"ONLINE"}'

# Test Lab 5: Send command
curl -X POST http://localhost:8080/api/devices/1/command \
  -H "Content-Type: application/json" \
  -d '{"action":"ON"}'

# Test Lab 6: System overview
curl http://localhost:8080/api/monitoring/overview

# Test Lab 7: Archive statistics
curl http://localhost:8080/api/data-optimization/archive/statistics
```

## ⚙️ Configuration

Chỉnh sửa `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/iotdb
spring.datasource.username=iotuser
spring.datasource.password=secret

# MQTT Broker
mqtt.host=localhost
mqtt.port=1883
mqtt.username=
mqtt.password=
```

## 🐛 Troubleshooting

### ⚠️ Java/Maven Issues (Phổ biến nhất)

| Problem                             | Solution                                                                                            |
| ----------------------------------- | --------------------------------------------------------------------------------------------------- |
| **JAVA_HOME not defined correctly** | `export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-17"` <br> Hoặc dùng: `mvn spring-boot:run` |
| **Maven wrapper lỗi**               | Dùng system Maven: `mvn spring-boot:run`                                                            |
| **Java not found**                  | Install JDK 17+: [Adoptium OpenJDK](https://adoptium.net/)                                          |
| **Permission denied ./mvnw**        | `chmod +x mvnw` (Linux/Mac) <br> Hoặc dùng `mvn` thay vì `./mvnw`                                   |

### Common Issues

| Problem                        | Solution                                                                |
| ------------------------------ | ----------------------------------------------------------------------- |
| **App không start**            | Kiểm tra PostgreSQL đã chạy: `docker-compose ps` hoặc manual setup      |
| **MQTT connection failed**     | Kiểm tra Mosquitto container: `docker-compose logs mosquitto`           |
| **404 API errors**             | Đảm bảo dùng đúng URL `http://localhost:8080`                           |
| **500 Database errors**        | Check database: `docker-compose exec postgres psql -U iotuser -d iotdb` |
| **Port conflicts (5432/1883)** | Stop existing services hoặc đổi ports trong docker-compose.yml          |

### Docker Commands hữu ích

```bash
# Stop tất cả containers
docker-compose down

# Stop và xóa volumes (reset data)
docker-compose down -v

# Restart services
docker-compose restart

# Xem logs real-time
docker-compose logs -f

# Exec vào container
docker-compose exec postgres bash
docker-compose exec mosquitto sh
```

### Logs để debug

```bash
# Xem logs chi tiết
./mvnw spring-boot:run -Dlogging.level.com.example.iot_backend=DEBUG

# Xem MQTT connection logs
./mvnw spring-boot:run -Dlogging.level.com.example.iot_backend.mqtt=DEBUG
```

## 📊 Database Schema

App tự động tạo tables:

- `device` - Thông tin devices
- `telemetry` - Dữ liệu cảm biến
- `device_status` - Trạng thái devices
- `telemetry_archive` - Dữ liệu đã nén

## 🚢 Production Deployment

1. **Package application:**

```bash
mvn clean package -DskipTests
```

2. **Run with production profile:**

```bash
java -jar target/iot-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

3. **Environment variables:**

```bash
export DB_URL=jdbc:postgresql://your-db-host:5432/iotdb
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export MQTT_HOST=your-mqtt-broker
```

## 🤝 Contributing

1. Fork the project
2. Create feature branch (`git checkout -b feature/new-lab`)
3. Commit changes (`git commit -am 'Add new lab'`)
4. Push to branch (`git push origin feature/new-lab`)
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

Nếu gặp vấn đề, hãy:

1. Check file `IoT_Labs_Testing_Guide.md`
2. Xem phần Troubleshooting ở trên
3. Tạo Issue trên GitHub repository

---

**Happy IoT Development! 🎉**

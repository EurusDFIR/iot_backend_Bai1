# 🧪 Hướng dẫn Test Tối ưu 7 Labs IoT Backend

## Môi trường cần chuẩn bị

- Spring Boot app chạy trên `localhost:8080`
- PostgreSQL database đã setup
- Mosquitto MQTT Broker chạy trên `localhost:1883`
- Postman để test APIs
- MQTT Explorer để publish/subscribe messages

---

## 📋 Lab 1: Spring Boot Backend + Database

### Test Database Connection

```bash
# Kiểm tra app khởi động thành công
curl http://localhost:8080/api/devices
```

**Kết quả mong đợi:** Response JSON array (có thể rỗng)

---

## 📋 Lab 2: MQTT Client Integration

### Test MQTT Connection

1. **Kiểm tra log app:** Thấy message `MQTT connected as clientId=...`

2. **Test với MQTT Explorer:**
   - Connect to `localhost:1883`
   - Publish message:
     - Topic: `iot/demo/temp`
     - Message: `{"temp": 25.5}`
   - Kiểm tra console app có log nhận message

---

## 📋 Lab 3: Device CRUD API

### Postman Collection

```
GET    /api/devices              # Lấy danh sách
POST   /api/devices              # Tạo mới
GET    /api/devices/{id}         # Lấy theo ID
PUT    /api/devices/{id}         # Cập nhật
DELETE /api/devices/{id}         # Xóa
```

### ⚠️ Common Issues Lab 3

| Problem            | URL Used                         | Correct URL                           | Solution                         |
| ------------------ | -------------------------------- | ------------------------------------- | -------------------------------- |
| 404 IIS Error      | `http://localhost/api/devices/1` | `http://localhost:8080/api/devices/1` | **Always use port 8080**         |
| 500 Internal Error | Correct URL                      | Check Spring Boot logs                | Restart app, check device exists |
| 404 Not Found      | `PUT /api/devices/999`           | Use existing device ID                | Create device first with POST    |

### Test Sequence (ĐÚNG THỨ TỰ)

1. **🏗️ Tạo device trước (POST):**

   ```
   POST http://localhost:8080/api/devices
   Content-Type: application/json

   {
     "name": "ESP32-Sensor-01",
     "type": "sensor",
     "status": "ONLINE"
   }
   ```

   **Response:** Sẽ trả về device với `id` (ví dụ: `{"id": 1, "name": "ESP32-Sensor-01", ...}`)

2. **📋 Kiểm tra danh sách:**

   ```
   GET http://localhost:8080/api/devices
   ```

3. **🔍 Lấy device theo ID (dùng ID từ bước 1):**

   ```
   GET http://localhost:8080/api/devices/1
   ```

4. **✏️ Cập nhật device (PUT):**

   ```
   PUT http://localhost:8080/api/devices/1
   Content-Type: application/json

   {
     "name": "ESP32-Sensor-01-Updated",
     "type": "sensor",
     "status": "OFFLINE"
   }
   ```

5. **🗑️ Xóa device:**
   ```
   DELETE http://localhost:8080/api/devices/1
   ```

---

## 📋 Lab 4: Telemetry Save to DB

### Prerequisites

- Tạo device trước: `POST /api/devices` (get device ID)

### Test Flow

1. **MQTT Explorer publish:**

   - Topic: `iot/device/1/telemetry`
   - Message: `{"temp": 26.5, "hum": 65.2}`

2. **Verify DB:**

   ```sql
   SELECT * FROM telemetry ORDER BY ts DESC LIMIT 5;
   ```

3. **Check app logs:** Message received và saved

---

## 📋 Lab 5: Device Command API

### ⚠️ Prerequisites cho Lab 5

1. **Device phải tồn tại:** Dùng `GET /api/devices` để lấy ID device hợp lệ
2. **MQTT connection hoạt động:** App đã connect MQTT thành công (check console logs)

### Test Command Flow

1. **Tạo device trước (nếu chưa có):**

   ```
   POST http://localhost:8080/api/devices
   Content-Type: application/json

   {
     "name": "ESP32-Command-Test",
     "type": "sensor",
     "status": "ONLINE"
   }
   ```

   **Response:** Lưu `id` để dùng cho command (ví dụ: `{"id": 1, ...}`)

2. **Setup device subscriber** (MQTT Explorer subscribe)

   - Topic: `iot/device/1/command` (thay 1 bằng device ID thực tế)

3. **Postman send command:**

   ```
   POST http://localhost:8080/api/devices/1/command
   Content-Type: application/json

   {"action": "ON"}
   ```

   **Response mong đợi:**

   ```json
   {
     "status": "success",
     "message": "Command sent to device 1",
     "topic": "iot/device/1/command",
     "data": { "action": "ON" }
   }
   ```

4. **Verify:** MQTT Explorer nhận được message tại topic `iot/device/1/command`

### 🐛 Lab 5 Troubleshooting

| Error                       | Cause                                    | Solution                                        |
| --------------------------- | ---------------------------------------- | ----------------------------------------------- |
| **404 Not Found**           | Device Controller thiếu command endpoint | ✅ Fixed - endpoint đã được thêm                |
| **Device not found**        | Device ID không tồn tại                  | Tạo device trước, hoặc check `GET /api/devices` |
| **MQTT not connected**      | App chưa connect MQTT                    | Restart app, check Mosquitto broker chạy        |
| **Command không được nhận** | MQTT Explorer subscribe sai topic        | Topic đúng: `iot/device/{id}/command`           |

---

## 📋 Lab 6: Dynamic Subscribe & Monitoring

### API Endpoints

```
POST /api/mqtt/subscribe         # Đăng ký topic
GET  /api/mqtt/subscriptions     # List topics đang subscribe
GET  /api/mqtt/recent           # Xem messages gần nhất
POST /api/mqtt/unsubscribe      # Hủy đăng ký
```

### Test Sequence

1. **Subscribe topic:**

   ```json
   POST /api/mqtt/subscribe
   {"topic": "iot/device/+/telemetry"}
   ```

2. **Publish data** (MQTT Explorer):

   - Topic: `iot/device/2/telemetry`
   - Message: `{"temp": 28.1}`

3. **Check recent messages:**
   ```
   GET /api/mqtt/recent?topic=iot/device/2/telemetry&limit=10
   ```

---

## 📋 Lab 7: Data Storage Optimization

### Archive APIs

```
GET  /api/data-optimization/archive/statistics
GET  /api/data-optimization/storage/usage
GET  /api/data-optimization/recommendations
POST /api/data-optimization/archive/force?deviceId=1
POST /api/data-optimization/archive/old-data?daysOld=30
POST /api/data-optimization/archive/cleanup
GET  /api/data-optimization/archive/data?deviceId=1&startDate=...&endDate=...
```

### Test Flow

1. **Tạo test data:**
   - Publish nhiều telemetry messages cho device
2. **Test statistics:**

   ```
   GET /api/data-optimization/archive/statistics
   ```

3. **Force archive:**

   ```
   POST /api/data-optimization/archive/force?deviceId=1
   ```

4. **Check compressed data:**
   ```sql
   SELECT * FROM telemetry_archive;
   ```

---

## 🔧 Quick Test Script (cURL)

```bash
#!/bin/bash
BASE_URL="http://localhost:8080"

echo "=== Testing Lab 3: Device CRUD ==="
# Create device and capture ID
echo "Creating device..."
RESPONSE=$(curl -s -X POST $BASE_URL/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Test-Device","type":"sensor","status":"ONLINE"}')
echo $RESPONSE | jq '.'

# Extract device ID
DEVICE_ID=$(echo $RESPONSE | jq -r '.id')
echo "Device ID: $DEVICE_ID"

# Test GET by ID
echo -e "\nGetting device by ID..."
curl -s $BASE_URL/api/devices/$DEVICE_ID | jq '.'

# Test UPDATE
echo -e "\nUpdating device..."
curl -s -X PUT $BASE_URL/api/devices/$DEVICE_ID \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated-Device","type":"sensor","status":"OFFLINE"}' | jq '.'

echo -e "\n=== Testing Lab 5: Send Command ==="
# Send command (use the device ID from above)
echo "Sending command to device $DEVICE_ID..."
curl -X POST $BASE_URL/api/devices/$DEVICE_ID/command \
  -H "Content-Type: application/json" \
  -d '{"action":"ON"}' | jq '.'

echo -e "\n=== Testing Lab 6: Subscribe ==="
# Subscribe topic
curl -X POST $BASE_URL/api/mqtt/subscribe \
  -H "Content-Type: application/json" \
  -d '{"topic":"iot/device/+/telemetry"}'

echo -e "\n=== Testing Lab 7: Archive Stats ==="
# Get archive statistics
curl $BASE_URL/api/data-optimization/archive/statistics
```

---

## 🐛 Common Issues

| Problem                           | Solution                                                  |
| --------------------------------- | --------------------------------------------------------- |
| **Wrong URL (missing :8080)**     | Always use `http://localhost:8080` not `http://localhost` |
| **500 Internal Server Error**     | Check Spring Boot app logs, restart application           |
| **404 Not Found for existing ID** | Verify device exists with `GET /api/devices` first        |
| **IIS Error Page**                | You're hitting port 80 instead of 8080                    |
| MQTT connection failed            | Check Mosquitto running on 1883                           |
| Device not found                  | Create device first via POST /api/devices                 |
| No telemetry data                 | Publish to correct topic: `iot/device/{id}/telemetry`     |
| Archive APIs return 500           | Check database schema created properly                    |
| Commands not received             | Verify topic pattern: `iot/device/{id}/command`           |

### 🔧 Quick Debug Commands

```bash
# Check if Spring Boot is running on 8080
curl http://localhost:8080/api/devices

# Check what's running on port 80 (should not be used)
curl http://localhost/

# List all devices to get valid IDs
curl http://localhost:8080/api/devices | jq '.'

# Create a test device and get its ID
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"name":"Debug-Device","type":"sensor","status":"ONLINE"}' | jq '.id'
```

---

## ✅ Success Criteria

- **Lab 1:** ✅ App starts, DB connected
- **Lab 2:** ✅ MQTT messages logged in console
- **Lab 3:** ✅ CRUD operations work in Postman
- **Lab 4:** ✅ Telemetry data saved to DB
- **Lab 5:** ✅ Commands published via MQTT
- **Lab 6:** ✅ Dynamic subscription working
- **Lab 7:** ✅ Data archiving and compression working

---

## 📱 Postman Collection Import

```json
{
  "info": { "name": "IoT Backend Labs" },
  "item": [
    {
      "name": "Lab 3 - Devices",
      "item": [
        {
          "name": "Get Devices",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/devices"
          }
        },
        {
          "name": "Create Device",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/devices",
            "header": [{ "key": "Content-Type", "value": "application/json" }],
            "body": {
              "raw": "{\"name\":\"ESP32-01\",\"type\":\"sensor\",\"status\":\"ONLINE\"}"
            }
          }
        }
      ]
    }
  ],
  "variable": [{ "key": "baseUrl", "value": "http://localhost:8080" }]
}
```

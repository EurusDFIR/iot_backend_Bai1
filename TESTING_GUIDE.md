# 🧪 IoT Backend - Testing Guide Chi Tiết

## Mục lục

- [🔧 Setup Tools](#setup-tools)
- [📋 Postman Collections](#postman-collections)
- [📡 MQTT Explorer Setup](#mqtt-explorer-setup)
- [🎯 Test Scenarios](#test-scenarios)
- [⚡ Performance Testing](#performance-testing)
- [🐛 Troubleshooting](#troubleshooting)

---

## 🔧 Setup Tools

### Yêu cầu cần thiết:

1. **Docker Desktop** - Đã cài và chạy project
2. **Postman** - https://www.postman.com/downloads/
3. **MQTT Explorer** - http://mqtt-explorer.com/
4. **curl** (optional) - Có sẵn trong Windows 10+

### Kiểm tra project đã chạy:

```bash
# Kiểm tra containers
docker ps

# Test API
curl http://localhost:8080/api/devices

# Expected: Status 200, JSON array response
```

---

## 📋 Postman Collections

### Import Collection JSON

**File: `IoT_Backend_Test_Collection.json`**

```json
{
  "info": {
    "name": "IoT Backend - Complete Test Suite",
    "description": "Comprehensive testing for all Lab 1-7 features",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "device_id",
      "value": "1",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "🏠 Health Check",
      "item": [
        {
          "name": "Server Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/actuator/health",
              "host": ["{{base_url}}"],
              "path": ["actuator", "health"]
            }
          },
          "test": [
            "pm.test('Server is UP', function () {",
            "    pm.response.to.have.status(200);",
            "    const response = pm.response.json();",
            "    pm.expect(response.status).to.eql('UP');",
            "});"
          ]
        },
        {
          "name": "Monitoring Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/monitoring/health",
              "host": ["{{base_url}}"],
              "path": ["api", "monitoring", "health"]
            }
          }
        }
      ]
    },
    {
      "name": "📱 Device Management (Lab 3)",
      "item": [
        {
          "name": "Get All Devices",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/devices",
              "host": ["{{base_url}}"],
              "path": ["api", "devices"]
            }
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
          "name": "Create Smart Thermostat",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Smart Thermostat {{$randomInt}}\",\n  \"type\": \"SENSOR\",\n  \"location\": \"Living Room\",\n  \"description\": \"Temperature and humidity control\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/devices",
              "host": ["{{base_url}}"],
              "path": ["api", "devices"]
            }
          },
          "test": [
            "pm.test('Device created successfully', function () {",
            "    pm.response.to.have.status(201);",
            "    const device = pm.response.json();",
            "    pm.expect(device).to.have.property('id');",
            "    pm.environment.set('device_id', device.id);",
            "    console.log('Created device ID: ' + device.id);",
            "});"
          ]
        },
        {
          "name": "Get Device by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/devices/{{device_id}}",
              "host": ["{{base_url}}"],
              "path": ["api", "devices", "{{device_id}}"]
            }
          },
          "test": [
            "pm.test('Device found', function () {",
            "    pm.response.to.have.status(200);",
            "    const device = pm.response.json();",
            "    pm.expect(device.id).to.eql(parseInt(pm.environment.get('device_id')));",
            "});"
          ]
        },
        {
          "name": "Update Device Location",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Smart Thermostat Pro\",\n  \"type\": \"SENSOR\",\n  \"location\": \"Master Bedroom\",\n  \"description\": \"Updated location and model\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/devices/{{device_id}}",
              "host": ["{{base_url}}"],
              "path": ["api", "devices", "{{device_id}}"]
            }
          }
        }
      ]
    },
    {
      "name": "📊 Telemetry (Lab 4)",
      "item": [
        {
          "name": "Create Telemetry Data",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"deviceId\": {{device_id}},\n  \"temperature\": {{$randomFloat}},\n  \"humidity\": {{$randomInt}},\n  \"timestamp\": \"{{$isoTimestamp}}\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/telemetry",
              "host": ["{{base_url}}"],
              "path": ["api", "telemetry"]
            }
          }
        },
        {
          "name": "Get Device Telemetry",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/devices/{{device_id}}/telemetry",
              "host": ["{{base_url}}"],
              "path": ["api", "devices", "{{device_id}}", "telemetry"]
            }
          }
        },
        {
          "name": "Get All Telemetry",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/telemetry",
              "host": ["{{base_url}}"],
              "path": ["api", "telemetry"]
            }
          }
        }
      ]
    },
    {
      "name": "⚡ Commands (Lab 5)",
      "item": [
        {
          "name": "Send SET_TEMP Command",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"command\": \"SET_TEMP\",\n  \"value\": \"{{$randomFloat}}\",\n  \"description\": \"Set target temperature\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/devices/{{device_id}}/command",
              "host": ["{{base_url}}"],
              "path": ["api", "devices", "{{device_id}}", "command"]
            }
          },
          "test": [
            "pm.test('Command sent successfully', function () {",
            "    pm.response.to.have.status(200);",
            "    const response = pm.response.json();",
            "    pm.expect(response).to.have.property('id');",
            "    pm.environment.set('command_id', response.id);",
            "});"
          ]
        },
        {
          "name": "Send REBOOT Command",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"command\": \"REBOOT\",\n  \"description\": \"Restart device\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/devices/{{device_id}}/command",
              "host": ["{{base_url}}"],
              "path": ["api", "devices", "{{device_id}}", "command"]
            }
          }
        },
        {
          "name": "Get All Commands",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/commands",
              "host": ["{{base_url}}"],
              "path": ["api", "commands"]
            }
          }
        }
      ]
    },
    {
      "name": "📈 Monitoring (Lab 6)",
      "item": [
        {
          "name": "System Overview",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/monitoring/overview",
              "host": ["{{base_url}}"],
              "path": ["api", "monitoring", "overview"]
            }
          },
          "test": [
            "pm.test('System overview data', function () {",
            "    const response = pm.response.json();",
            "    pm.expect(response).to.have.property('totalDevices');",
            "    pm.expect(response).to.have.property('onlineDevices');",
            "    pm.expect(response).to.have.property('offlineDevices');",
            "});"
          ]
        },
        {
          "name": "Device Status List",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/monitoring/devices",
              "host": ["{{base_url}}"],
              "path": ["api", "monitoring", "devices"]
            }
          }
        },
        {
          "name": "Mark Device Online",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/monitoring/devices/{{device_id}}/online",
              "host": ["{{base_url}}"],
              "path": [
                "api",
                "monitoring",
                "devices",
                "{{device_id}}",
                "online"
              ]
            }
          }
        },
        {
          "name": "Dashboard Data",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/monitoring/dashboard",
              "host": ["{{base_url}}"],
              "path": ["api", "monitoring", "dashboard"]
            }
          }
        }
      ]
    }
  ]
}
```

### Cách sử dụng Collection:

1. **Mở Postman** → **Import** → Paste JSON trên
2. **Tạo Environment:**
   - Name: `IoT Local`
   - Variables:
     - `base_url`: `http://localhost:8080`
     - `device_id`: `1`
3. **Run Collection:** Collections → Run → Select all tests
4. **Xem kết quả:** Test Results tab

---

## 📡 MQTT Explorer Setup

### Connection Configuration

**Basic Settings:**

```
Host: localhost
Port: 1883
Protocol: mqtt://
Username: (empty)
Password: (empty)
Client ID: mqtt-explorer-{{$timestamp}}
```

**Advanced Settings:**

```
Keep Alive: 60 seconds
Clean Session: ✅
Auto Reconnect: ✅
Connect Timeout: 30 seconds
```

### Topic Structure Overview

```
iot/
├── devices/
│   ├── {deviceId}/
│   │   ├── telemetry      # 📊 Device → Server (sensor data)
│   │   ├── command        # ⚡ Server → Device (control commands)
│   │   └── status         # 💓 Device heartbeat/status
│   └── broadcast/
│       └── command        # 📢 Server → All devices
└── system/
    ├── health             # ❤️ System health checks
    └── monitoring         # 📈 System metrics
```

### MQTT Test Messages

**1. Telemetry Data (Device → Server)**

```json
Topic: iot/devices/1/telemetry
QoS: 1
Retain: false
Payload:
{
  "deviceId": 1,
  "temperature": 23.5,
  "humidity": 55.2,
  "pressure": 1013.25,
  "timestamp": "2024-01-15T10:30:00Z",
  "battery": 87,
  "signal": -45
}
```

**2. Command Response (Device → Server)**

```json
Topic: iot/devices/1/command/response
QoS: 1
Payload:
{
  "commandId": "cmd-123",
  "deviceId": 1,
  "status": "SUCCESS",
  "message": "Temperature set to 24.0°C",
  "timestamp": "2024-01-15T10:31:00Z"
}
```

**3. Device Status (Device → Server)**

```json
Topic: iot/devices/1/status
QoS: 1
Retain: true
Payload:
{
  "deviceId": 1,
  "status": "ONLINE",
  "lastSeen": "2024-01-15T10:30:00Z",
  "uptime": 3600,
  "firmwareVersion": "1.2.3",
  "wifiSignal": -42
}
```

**4. Broadcast Command Test**

```json
Topic: iot/devices/broadcast/command
QoS: 1
Payload:
{
  "command": "SYNC_TIME",
  "timestamp": "2024-01-15T10:30:00Z",
  "value": "2024-01-15T10:30:00Z"
}
```

---

## 🎯 Test Scenarios

### Scenario A: Device Registration & First Data

**Objective:** Test complete device onboarding flow

**Steps:**

1. **Create device via Postman** (Device Management → Create Smart Thermostat)
2. **Note device ID** từ response (auto-saved to environment)
3. **Subscribe MQTT topics:**
   - `iot/devices/+/telemetry`
   - `iot/devices/+/command`
   - `iot/devices/+/status`
4. **Simulate device sending first telemetry:**
   ```json
   Topic: iot/devices/{device_id}/telemetry
   {
     "deviceId": {device_id},
     "temperature": 22.1,
     "humidity": 45.0,
     "timestamp": "2024-01-15T10:00:00Z"
   }
   ```
5. **Verify data in database** (Postman → Get Device Telemetry)
6. **Send command to device** (Postman → Send SET_TEMP Command)
7. **Observe command in MQTT Explorer** (topic: `iot/devices/{device_id}/command`)

**Expected Results:**

- ✅ Device created with unique ID
- ✅ Telemetry data appears in MQTT Explorer
- ✅ Data saved to database (visible via API)
- ✅ Command sent via MQTT
- ✅ Monitoring shows device as online

### Scenario B: Multi-Device Network

**Objective:** Test system with multiple devices

**Setup:**

1. **Create 3 different devices:**

   - Temperature Sensor
   - Humidity Sensor
   - Smart Light Controller

2. **Each device sends different data types:**

   ```bash
   # Device 1 - Temperature readings
   iot/devices/1/telemetry: {"deviceId":1,"temperature":25.5,"timestamp":"..."}

   # Device 2 - Humidity readings
   iot/devices/2/telemetry: {"deviceId":2,"humidity":60.0,"timestamp":"..."}

   # Device 3 - Light control status
   iot/devices/3/telemetry: {"deviceId":3,"brightness":75,"color":"warm","timestamp":"..."}
   ```

3. **Test system monitoring:**

   ```http
   GET /api/monitoring/overview
   Expected response:
   {
     "totalDevices": 3,
     "onlineDevices": 3,
     "offlineDevices": 0,
     ...
   }
   ```

4. **Test broadcast command:**
   ```json
   Topic: iot/devices/broadcast/command
   {
     "command": "HEALTH_CHECK",
     "timestamp": "2024-01-15T10:05:00Z"
   }
   ```

**Verification:**

- ✅ All devices appear in monitoring dashboard
- ✅ Each device receives broadcast command
- ✅ System statistics are accurate
- ✅ No data cross-contamination between devices

### Scenario C: Error Handling & Edge Cases

**Test invalid scenarios:**

1. **Invalid device ID:**

   ```http
   GET /api/devices/999999
   Expected: 404 Not Found
   ```

2. **Malformed JSON in MQTT:**

   ```
   Topic: iot/devices/1/telemetry
   Payload: "this is not json"
   Expected: No crash, error logged
   ```

3. **Command to non-existent device:**

   ```http
   POST /api/devices/999/command
   Expected: 400 Bad Request
   ```

4. **Empty telemetry data:**
   ```json
   Topic: iot/devices/1/telemetry
   Payload: {}
   Expected: Handled gracefully
   ```

---

## ⚡ Performance Testing

### Load Testing with curl

**Script: `load_test.sh`**

```bash
#!/bin/bash
echo "Starting load test..."

# Create 10 devices rapidly
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/api/devices \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"LoadTest Device $i\",\"type\":\"SENSOR\",\"location\":\"Test Lab\"}" &
done
wait

echo "Created 10 devices"

# Send 100 telemetry points
for i in {1..100}; do
  DEVICE_ID=$((1 + $i % 10))
  TEMP=$(echo "20 + $i % 10" | bc -l)

  curl -s -X POST http://localhost:8080/api/telemetry \
    -H "Content-Type: application/json" \
    -d "{\"deviceId\":$DEVICE_ID,\"temperature\":$TEMP,\"timestamp\":\"$(date -Iseconds)\"}" &

  if [ $((i % 10)) -eq 0 ]; then
    wait
    echo "Sent $i telemetry points..."
  fi
done

echo "Load test completed!"
```

### MQTT Stress Test

**Python script: `mqtt_stress.py`**

```python
import paho.mqtt.client as mqtt
import json
import time
import threading
from datetime import datetime

def mqtt_publisher(device_id, num_messages):
    client = mqtt.Client(f"stress_test_{device_id}")
    client.connect("localhost", 1883, 60)

    for i in range(num_messages):
        data = {
            "deviceId": device_id,
            "temperature": 20 + (i % 20),
            "humidity": 40 + (i % 40),
            "timestamp": datetime.utcnow().isoformat() + "Z"
        }

        topic = f"iot/devices/{device_id}/telemetry"
        client.publish(topic, json.dumps(data), qos=1)
        time.sleep(0.1)  # 10 messages per second

    client.disconnect()
    print(f"Device {device_id} sent {num_messages} messages")

# Start 5 devices, each sending 100 messages
threads = []
for device_id in range(1, 6):
    t = threading.Thread(target=mqtt_publisher, args=(device_id, 100))
    threads.append(t)
    t.start()

# Wait for all threads
for t in threads:
    t.join()

print("Stress test completed!")
```

### Performance Metrics to Monitor

**During load testing, check:**

1. **API Response Times:**

   ```bash
   curl -w "@curl-format.txt" -s -o /dev/null http://localhost:8080/api/devices
   # curl-format.txt contains: time_total:%{time_total}
   ```

2. **Database Performance:**

   ```sql
   -- Connect to database and run:
   SELECT COUNT(*) FROM telemetry;
   SELECT COUNT(*) FROM devices;
   SELECT COUNT(*) FROM commands;
   ```

3. **Container Resource Usage:**

   ```bash
   docker stats --no-stream
   ```

4. **MQTT Connection Count:**
   ```bash
   # In MQTT Explorer, check connection status
   # Monitor for connection drops
   ```

**Expected Performance:**

- ✅ API responses < 500ms under normal load
- ✅ Database queries < 100ms
- ✅ MQTT messages processed in real-time
- ✅ Memory usage stable under load
- ✅ No connection leaks

---

## 🐛 Troubleshooting

### Common Issues & Solutions

**1. MQTT Explorer Can't Connect**

```
Error: Connection failed
Solutions:
- Run: docker ps (check mosquitto-iot-simple running)
- Run: docker logs mosquitto-iot-simple
- Use: mqtt-guide.bat for step-by-step setup
- Try WebSocket: ws://localhost:9001
```

**2. Postman Tests Failing**

```
Error: 404 Not Found / Connection refused
Solutions:
- Check: http://localhost:8080/actuator/health
- Run: docker logs iot-backend-simple
- Restart: docker-compose -f docker-compose-simple.yml restart
- Verify: Port 8080 not blocked by firewall
```

**3. Database Connection Issues**

```
Error: org.postgresql.util.PSQLException
Solutions:
- Check: docker logs postgres-iot-simple
- Verify: Database initialized (check data.sql)
- Reset: docker-compose down -v && start.bat
```

**4. Missing/Corrupted Data**

```
Issue: API returns empty arrays
Solutions:
- Check database: docker exec -it postgres-iot-simple psql -U iotuser -d iotdb
- Run queries: SELECT * FROM devices; SELECT * FROM telemetry;
- Reinitialize: docker-compose down -v && start.bat
```

**5. MQTT Messages Not Appearing**

```
Issue: Published but not received
Solutions:
- Verify topic names match exactly
- Check QoS levels (use QoS 1)
- Test with mosquitto_pub:
  docker exec mosquitto-iot-simple mosquitto_pub -t "test/topic" -m "test message"
- Check container networking
```

### Debug Commands

**Container Health:**

```bash
# Check all containers
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Check specific logs
docker logs iot-backend-simple --tail 50
docker logs mosquitto-iot-simple --tail 50
docker logs postgres-iot-simple --tail 50

# Check container resource usage
docker stats --no-stream
```

**Database Debug:**

```bash
# Connect to database
docker exec -it postgres-iot-simple psql -U iotuser -d iotdb

# Check table contents
\dt                                    # List tables
SELECT COUNT(*) FROM devices;         # Count devices
SELECT * FROM devices LIMIT 5;       # Sample devices
SELECT COUNT(*) FROM telemetry;      # Count telemetry
SELECT * FROM telemetry ORDER BY timestamp DESC LIMIT 5;  # Latest telemetry
```

**Network Connectivity:**

```bash
# Test API endpoint
curl -I http://localhost:8080/api/devices

# Test MQTT connectivity
telnet localhost 1883

# Test PostgreSQL connectivity
telnet localhost 5432

# Check port usage
netstat -an | findstr :8080
netstat -an | findstr :1883
netstat -an | findstr :5432
```

### Logging & Monitoring

**Application Logs:**

```bash
# Tail live logs
docker logs -f iot-backend-simple

# Search for errors
docker logs iot-backend-simple 2>&1 | grep -i error

# Check startup sequence
docker logs iot-backend-simple | grep -i "started\|completed\|failed"
```

**System Health Check:**

```bash
# Full system check script
echo "=== IoT Backend Health Check ==="
echo "1. Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}"

echo -e "\n2. API Health:"
curl -s http://localhost:8080/actuator/health | jq .

echo -e "\n3. Database Connection:"
docker exec postgres-iot-simple pg_isready -U iotuser

echo -e "\n4. MQTT Broker:"
timeout 5 telnet localhost 1883 < /dev/null && echo "MQTT OK" || echo "MQTT FAILED"

echo -e "\n5. Data Counts:"
docker exec postgres-iot-simple psql -U iotuser -d iotdb -c "SELECT 'Devices:' as table_name, COUNT(*) as count FROM devices UNION ALL SELECT 'Telemetry:', COUNT(*) FROM telemetry UNION ALL SELECT 'Commands:', COUNT(*) FROM commands;"
```

---

## 📊 Test Results Dashboard

### Manual Testing Checklist

**Basic Functionality:**

- [ ] ✅ Docker containers start successfully
- [ ] ✅ API health endpoint responds (200 OK)
- [ ] ✅ MQTT Explorer connects to broker
- [ ] ✅ Database initialized with sample data
- [ ] ✅ Postman collection imports successfully

**Device Management (Lab 3):**

- [ ] ✅ Create device via API
- [ ] ✅ Retrieve device by ID
- [ ] ✅ Update device information
- [ ] ✅ List all devices
- [ ] ✅ Error handling for invalid IDs

**Telemetry (Lab 4):**

- [ ] ✅ MQTT messages received and stored
- [ ] ✅ Telemetry data retrievable via API
- [ ] ✅ Data validation works correctly
- [ ] ✅ Timestamp handling accurate

**Commands (Lab 5):**

- [ ] ✅ Commands sent via API
- [ ] ✅ Commands appear in MQTT broker
- [ ] ✅ Command history stored
- [ ] ✅ Command status tracking

**Monitoring (Lab 6):**

- [ ] ✅ System overview shows correct stats
- [ ] ✅ Device status tracking works
- [ ] ✅ Dashboard data is accurate
- [ ] ✅ Real-time updates function

**Integration & Performance:**

- [ ] ✅ End-to-end workflow complete
- [ ] ✅ Multiple devices supported
- [ ] ✅ Performance acceptable under load
- [ ] ✅ Error recovery works
- [ ] ✅ Data consistency maintained

---

**🎉 Testing hoàn tất! Project sẵn sàng cho production deployment.**

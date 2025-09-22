package com.example.iot_backend.controller;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.service.DeviceService;
import com.example.iot_backend.service.TelemetryService;
import com.example.iot_backend.mqtt.HiveMqttService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;
    private final TelemetryService telemetryService;
    private final HiveMqttService mqttService;
    private final ObjectMapper objectMapper;

    public DeviceController(DeviceService service, TelemetryService telemetryService,
            HiveMqttService mqttService, ObjectMapper objectMapper) {
        this.service = service;
        this.telemetryService = telemetryService;
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
    }

    // GET: danh sách thiết bị
    @GetMapping
    public List<Device> getAllDevices() {
        return service.findAll();
    }

    // GET: 1 thiết bị theo id
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: thêm mới thiết bị
    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        // Reset id để đảm bảo tạo mới
        device.setId(null);
        return service.save(device);
    }

    // PUT: cập nhật thiết bị
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return service.findById(id)
                .map(existing -> {
                    existing.setName(device.getName());
                    existing.setType(device.getType());
                    existing.setStatus(device.getStatus());
                    return ResponseEntity.ok(service.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: xóa thiết bị
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        if (service.existsById(id)) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Lấy telemetry của device (Bài tập 2)
    @GetMapping("/{id}/telemetry")
    public List<Telemetry> getDeviceTelemetry(@PathVariable Long id) {
        return telemetryService.getTelemetryByDeviceId(id);
    }

    // POST: Send command to device (Lab 5)
    @PostMapping("/{id}/command")
    public ResponseEntity<?> sendCommand(@PathVariable Long id, @RequestBody Map<String, Object> commandData) {
        try {
            // Check if device exists
            if (!service.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Create topic for device command
            String topic = String.format("iot/device/%d/command", id);

            // Convert command data to JSON string
            String commandJson = objectMapper.writeValueAsString(commandData);

            // Publish command via MQTT
            boolean success = mqttService.publishMessage(topic, commandJson);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Command sent to device " + id,
                        "topic", topic,
                        "data", commandData));
            } else {
                return ResponseEntity.internalServerError().body(Map.of(
                        "status", "error",
                        "message", "Failed to send command to device"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Error processing command: " + e.getMessage()));
        }
    }
}
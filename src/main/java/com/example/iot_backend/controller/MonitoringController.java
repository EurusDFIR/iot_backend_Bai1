package com.example.iot_backend.controller;

import com.example.iot_backend.model.DeviceStatus;
import com.example.iot_backend.model.DeviceStatusEnum;
import com.example.iot_backend.service.DeviceMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final DeviceMonitoringService monitoringService;

    public MonitoringController(DeviceMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Lấy tổng quan hệ thống
     */
    @GetMapping("/overview")
    public Map<String, Object> getSystemOverview() {
        return monitoringService.getSystemOverview();
    }

    /**
     * Lấy tất cả device status
     */
    @GetMapping("/devices")
    public List<DeviceStatus> getAllDeviceStatus() {
        return monitoringService.getAllDeviceStatus();
    }

    /**
     * Lấy device status theo ID
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceStatus> getDeviceStatus(@PathVariable Long deviceId) {
        Optional<DeviceStatus> status = monitoringService.getDeviceStatus(deviceId);
        return status.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy devices theo status
     */
    @GetMapping("/devices/status/{status}")
    public ResponseEntity<List<DeviceStatus>> getDevicesByStatus(@PathVariable String status) {
        try {
            DeviceStatusEnum deviceStatus = DeviceStatusEnum.valueOf(status.toUpperCase());
            List<DeviceStatus> devices = monitoringService.getDevicesByStatus(deviceStatus);
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy devices online
     */
    @GetMapping("/devices/online")
    public List<DeviceStatus> getOnlineDevices() {
        return monitoringService.getDevicesByStatus(DeviceStatusEnum.ONLINE);
    }

    /**
     * Lấy devices offline
     */
    @GetMapping("/devices/offline")
    public List<DeviceStatus> getOfflineDevices() {
        return monitoringService.getDevicesByStatus(DeviceStatusEnum.OFFLINE);
    }

    /**
     * Lấy top devices theo uptime
     */
    @GetMapping("/devices/top-uptime")
    public List<DeviceStatus> getTopUptimeDevices() {
        return monitoringService.getTopUptimeDevices();
    }

    /**
     * Lấy devices kết nối gần đây
     */
    @GetMapping("/devices/recent")
    public List<DeviceStatus> getRecentlyConnectedDevices(@RequestParam(defaultValue = "24") int hours) {
        return monitoringService.getRecentlyConnectedDevices(hours);
    }

    /**
     * Thống kê dashboard
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = monitoringService.getSystemOverview();

        // Thêm thống kê bổ sung
        dashboard.put("recentDevices", monitoringService.getRecentlyConnectedDevices(1));
        dashboard.put("topUptimeDevices", monitoringService.getTopUptimeDevices());

        return dashboard;
    }

    /**
     * Manually mark device online (for testing)
     */
    @PostMapping("/devices/{deviceId}/online")
    public ResponseEntity<Void> markDeviceOnline(@PathVariable Long deviceId) {
        try {
            monitoringService.markDeviceOnline(deviceId, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Manually mark device offline (for testing)
     */
    @PostMapping("/devices/{deviceId}/offline")
    public ResponseEntity<Void> markDeviceOffline(@PathVariable Long deviceId) {
        try {
            monitoringService.markDeviceOffline(deviceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now(),
                "monitoring", "active");
        return health;
    }
}
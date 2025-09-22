package com.example.iot_backend.service;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.DeviceStatus;
import com.example.iot_backend.model.DeviceStatusEnum;
import com.example.iot_backend.repository.DeviceRepository;
import com.example.iot_backend.repository.DeviceStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class DeviceMonitoringService {
    private static final Logger log = LoggerFactory.getLogger(DeviceMonitoringService.class);

    private static final int DEVICE_TIMEOUT_SECONDS = 300; // 5 minutes
    private static final int LOW_BATTERY_THRESHOLD = 20; // 20%
    private static final int WEAK_SIGNAL_THRESHOLD = -80; // -80 dBm

    private final DeviceStatusRepository deviceStatusRepo;
    private final DeviceRepository deviceRepo;

    // Cache subscribed topics để tránh subscribe trùng
    private final Set<String> subscribedTopics = new HashSet<>();

    public DeviceMonitoringService(DeviceStatusRepository deviceStatusRepo,
            DeviceRepository deviceRepo) {
        this.deviceStatusRepo = deviceStatusRepo;
        this.deviceRepo = deviceRepo;
    }

    /**
     * Đánh dấu device online và cập nhật heartbeat
     */
    public void markDeviceOnline(Long deviceId, Map<String, Object> metadata) {
        Optional<DeviceStatus> statusOpt = deviceStatusRepo.findByDeviceId(deviceId);
        DeviceStatus status;

        if (statusOpt.isPresent()) {
            status = statusOpt.get();
            boolean wasOffline = !status.isOnline();
            status.markOnline();

            if (wasOffline) {
                log.info("🟢 Device {} came ONLINE", deviceId);
            }
        } else {
            // Tạo status mới cho device
            Optional<Device> deviceOpt = deviceRepo.findById(deviceId);
            if (deviceOpt.isPresent()) {
                status = new DeviceStatus(deviceOpt.get());
                status.markOnline();
                log.info("🟢 Device {} registered and ONLINE", deviceId);
            } else {
                log.warn("Device {} not found in database", deviceId);
                return;
            }
        }

        // Cập nhật metadata nếu có
        updateDeviceMetadata(status, metadata);

        deviceStatusRepo.save(status);
    }

    /**
     * Cập nhật heartbeat cho device
     */
    public void updateHeartbeat(Long deviceId, Map<String, Object> metadata) {
        Optional<DeviceStatus> statusOpt = deviceStatusRepo.findByDeviceId(deviceId);
        if (statusOpt.isPresent()) {
            DeviceStatus status = statusOpt.get();
            status.updateHeartbeat();
            updateDeviceMetadata(status, metadata);
            deviceStatusRepo.save(status);
        } else {
            // Device chưa có status, tạo mới
            markDeviceOnline(deviceId, metadata);
        }
    }

    /**
     * Đánh dấu device offline
     */
    public void markDeviceOffline(Long deviceId) {
        Optional<DeviceStatus> statusOpt = deviceStatusRepo.findByDeviceId(deviceId);
        if (statusOpt.isPresent()) {
            DeviceStatus status = statusOpt.get();
            if (status.isOnline()) {
                status.markOffline();
                deviceStatusRepo.save(status);
                log.warn("🔴 Device {} went OFFLINE", deviceId);
            }
        }
    }

    /**
     * Cập nhật metadata cho device
     */
    private void updateDeviceMetadata(DeviceStatus status, Map<String, Object> metadata) {
        if (metadata == null)
            return;

        // Update firmware version
        if (metadata.containsKey("firmware")) {
            status.setFirmwareVersion(metadata.get("firmware").toString());
        }

        // Update IP address
        if (metadata.containsKey("ip")) {
            status.setIpAddress(metadata.get("ip").toString());
        }

        // Update signal strength
        if (metadata.containsKey("signal")) {
            try {
                status.setSignalStrength(Integer.valueOf(metadata.get("signal").toString()));
            } catch (NumberFormatException e) {
                log.debug("Invalid signal strength: {}", metadata.get("signal"));
            }
        }

        // Update battery level
        if (metadata.containsKey("battery")) {
            try {
                status.setBatteryLevel(Integer.valueOf(metadata.get("battery").toString()));
            } catch (NumberFormatException e) {
                log.debug("Invalid battery level: {}", metadata.get("battery"));
            }
        }
    }

    /**
     * Scheduled task: Kiểm tra devices timeout
     */
    @Scheduled(fixedRate = 60000) // Chạy mỗi 1 phút
    public void checkDeviceTimeouts() {
        Instant timeoutThreshold = Instant.now().minusSeconds(DEVICE_TIMEOUT_SECONDS);
        List<DeviceStatus> timedOutDevices = deviceStatusRepo.findTimedOutDevices(timeoutThreshold);

        for (DeviceStatus status : timedOutDevices) {
            markDeviceOffline(status.getDevice().getId());
        }

        if (!timedOutDevices.isEmpty()) {
            log.info("Found {} devices that timed out", timedOutDevices.size());
        }
    }

    /**
     * Scheduled task: Kiểm tra cảnh báo
     */
    @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
    public void checkDeviceAlerts() {
        // Kiểm tra battery thấp
        List<DeviceStatus> lowBatteryDevices = deviceStatusRepo.findLowBatteryDevices(LOW_BATTERY_THRESHOLD);
        for (DeviceStatus status : lowBatteryDevices) {
            log.warn("🔋 LOW BATTERY ALERT: Device {} battery: {}%",
                    status.getDevice().getId(), status.getBatteryLevel());
        }

        // Kiểm tra signal yếu
        List<DeviceStatus> weakSignalDevices = deviceStatusRepo.findWeakSignalDevices(WEAK_SIGNAL_THRESHOLD);
        for (DeviceStatus status : weakSignalDevices) {
            log.warn("📶 WEAK SIGNAL ALERT: Device {} signal: {} dBm",
                    status.getDevice().getId(), status.getSignalStrength());
        }
    }

    /**
     * Lấy trạng thái tổng quan hệ thống
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Đếm devices theo status
        List<Object[]> statusCounts = deviceStatusRepo.getDeviceStatusCounts();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        overview.put("deviceCounts", statusMap);

        // Devices online
        List<DeviceStatus> onlineDevices = deviceStatusRepo.findByStatus(DeviceStatusEnum.ONLINE);
        overview.put("onlineDevices", onlineDevices.size());

        // Devices offline
        List<DeviceStatus> offlineDevices = deviceStatusRepo.findByStatus(DeviceStatusEnum.OFFLINE);
        overview.put("offlineDevices", offlineDevices.size());

        // Alerts
        Map<String, Integer> alerts = new HashMap<>();
        alerts.put("lowBattery", deviceStatusRepo.findLowBatteryDevices(LOW_BATTERY_THRESHOLD).size());
        alerts.put("weakSignal", deviceStatusRepo.findWeakSignalDevices(WEAK_SIGNAL_THRESHOLD).size());
        overview.put("alerts", alerts);

        return overview;
    }

    /**
     * Lấy tất cả device status
     */
    public List<DeviceStatus> getAllDeviceStatus() {
        return deviceStatusRepo.findAll();
    }

    /**
     * Lấy device status theo ID
     */
    public Optional<DeviceStatus> getDeviceStatus(Long deviceId) {
        return deviceStatusRepo.findByDeviceId(deviceId);
    }

    /**
     * Lấy devices theo status
     */
    public List<DeviceStatus> getDevicesByStatus(DeviceStatusEnum status) {
        return deviceStatusRepo.findByStatusOrderByLastSeenDesc(status);
    }

    /**
     * Lấy top devices theo uptime
     */
    public List<DeviceStatus> getTopUptimeDevices() {
        return deviceStatusRepo.findTopUptimeDevices();
    }

    /**
     * Lấy devices kết nối gần đây
     */
    public List<DeviceStatus> getRecentlyConnectedDevices(int hours) {
        Instant since = Instant.now().minusSeconds(hours * 3600L);
        return deviceStatusRepo.findRecentlyConnectedDevices(since);
    }

    /**
     * Kiểm tra device có cần được subscribe không
     */
    public boolean shouldSubscribeToDevice(Long deviceId) {
        String topic = String.format("iot/device/%d/+", deviceId);
        return !subscribedTopics.contains(topic);
    }

    /**
     * Đánh dấu đã subscribe topic
     */
    public void markTopicSubscribed(String topic) {
        subscribedTopics.add(topic);
        log.debug("Marked topic as subscribed: {}", topic);
    }

    /**
     * Reset subscription cache (dùng khi restart)
     */
    public void resetSubscriptions() {
        subscribedTopics.clear();
        log.info("Reset subscription cache");
    }
}
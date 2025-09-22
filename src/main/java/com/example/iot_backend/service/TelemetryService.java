package com.example.iot_backend.service;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.repository.DeviceRepository;
import com.example.iot_backend.repository.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TelemetryService {
    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final TelemetryRepository telemetryRepo;
    private final DeviceRepository deviceRepo;

    public TelemetryService(TelemetryRepository telemetryRepo, DeviceRepository deviceRepo) {
        this.telemetryRepo = telemetryRepo;
        this.deviceRepo = deviceRepo;
    }

    public void saveTelemetry(Long deviceId, String jsonData) {
        Optional<Device> deviceOpt = deviceRepo.findById(deviceId);
        if (deviceOpt.isPresent()) {
            Telemetry telemetry = new Telemetry();
            telemetry.setDevice(deviceOpt.get());
            telemetry.setTs(Instant.now());
            telemetry.setData(jsonData);

            telemetryRepo.save(telemetry);
            log.info("Saved telemetry for device {}: {}", deviceId, jsonData);

            // Thêm logic cảnh báo nhiệt độ cao
            checkTemperatureAlert(jsonData);
        } else {
            log.warn("Device not found with id={}", deviceId);
        }
    }

    private void checkTemperatureAlert(String jsonData) {
        try {
            // Simple JSON parsing để tìm nhiệt độ
            if (jsonData.contains("temp")) {
                String tempStr = jsonData.replaceAll(".*\"temp\"\\s*:\\s*([0-9.-]+).*", "$1");
                double temp = Double.parseDouble(tempStr);
                if (temp > 30.0) {
                    log.warn("🔥 HIGH TEMPERATURE ALERT: {}°C", temp);
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse temperature from: {}", jsonData);
        }
    }

    public List<Telemetry> getAllTelemetry() {
        return telemetryRepo.findAll();
    }

    public List<Telemetry> getTelemetryByDeviceId(Long deviceId) {
        return telemetryRepo.findByDeviceIdOrderByTsDesc(deviceId);
    }
}
package com.example.iot_backend.dto;

import java.time.Instant;

/**
 * DTO for serializing Telemetry data for archiving
 */
public class TelemetryArchiveDTO {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private Instant ts;
    private String data;

    // Constructors
    public TelemetryArchiveDTO() {
    }

    public TelemetryArchiveDTO(Long id, Long deviceId, String deviceName, Instant ts, String data) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ts = ts;
        this.data = data;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
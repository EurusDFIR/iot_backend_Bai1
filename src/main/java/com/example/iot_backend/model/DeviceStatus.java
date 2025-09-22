package com.example.iot_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_status")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DeviceStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatusEnum status = DeviceStatusEnum.OFFLINE;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "connection_count")
    private Long connectionCount = 0L;

    @Column(name = "total_uptime_seconds")
    private Long totalUptimeSeconds = 0L;

    @Column(name = "last_connect_time")
    private Instant lastConnectTime;

    @Column(name = "last_disconnect_time")
    private Instant lastDisconnectTime;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "signal_strength")
    private Integer signalStrength; // WiFi signal strength (-100 to 0 dBm)

    @Column(name = "battery_level")
    private Integer batteryLevel; // 0-100%

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public DeviceStatus() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public DeviceStatus(Device device) {
        this();
        this.device = device;
    }

    // Helper methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void markOnline() {
        if (this.status == DeviceStatusEnum.OFFLINE) {
            this.lastConnectTime = Instant.now();
            this.connectionCount++;
        }
        this.status = DeviceStatusEnum.ONLINE;
        this.lastSeen = Instant.now();
        this.lastHeartbeat = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markOffline() {
        if (this.status == DeviceStatusEnum.ONLINE && this.lastConnectTime != null) {
            // Calculate uptime for this session
            long sessionUptime = Instant.now().getEpochSecond() - this.lastConnectTime.getEpochSecond();
            this.totalUptimeSeconds += sessionUptime;
        }
        this.status = DeviceStatusEnum.OFFLINE;
        this.lastDisconnectTime = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = Instant.now();
        this.lastSeen = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isOnline() {
        return this.status == DeviceStatusEnum.ONLINE;
    }

    public boolean hasTimedOut(int timeoutSeconds) {
        if (lastHeartbeat == null)
            return true;
        return Instant.now().getEpochSecond() - lastHeartbeat.getEpochSecond() > timeoutSeconds;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    // Getter cho deviceId để serialize JSON
    public Long getDeviceId() {
        return device != null ? device.getId() : null;
    }

    public DeviceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DeviceStatusEnum status) {
        this.status = status;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(Long connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Long getTotalUptimeSeconds() {
        return totalUptimeSeconds;
    }

    public void setTotalUptimeSeconds(Long totalUptimeSeconds) {
        this.totalUptimeSeconds = totalUptimeSeconds;
    }

    public Instant getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(Instant lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public Instant getLastDisconnectTime() {
        return lastDisconnectTime;
    }

    public void setLastDisconnectTime(Instant lastDisconnectTime) {
        this.lastDisconnectTime = lastDisconnectTime;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DeviceStatus{" +
                "id=" + id +
                ", device=" + (device != null ? device.getId() : null) +
                ", status=" + status +
                ", lastSeen=" + lastSeen +
                ", connectionCount=" + connectionCount +
                '}';
    }
}
package com.example.iot_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "telemetry_archive")
public class TelemetryArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "original_count", nullable = false)
    private Integer originalCount; // Số lượng record gốc

    @Column(name = "start_date", nullable = false)
    private Instant startDate; // Ngày bắt đầu của dữ liệu

    @Column(name = "end_date", nullable = false)
    private Instant endDate; // Ngày kết thúc của dữ liệu

    @Column(name = "archived_date", nullable = false)
    private Instant archivedDate; // Ngày archive

    @Column(name = "compressed_data")
    private byte[] compressedData; // Dữ liệu đã nén

    @Column(name = "compression_ratio")
    private Double compressionRatio; // Tỷ lệ nén

    @Column(name = "archive_type")
    @Enumerated(EnumType.STRING)
    private ArchiveType archiveType = ArchiveType.DAILY;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public enum ArchiveType {
        HOURLY, DAILY, WEEKLY, MONTHLY
    }

    // Constructors
    public TelemetryArchive() {
        this.createdAt = Instant.now();
        this.archivedDate = Instant.now();
    }

    public TelemetryArchive(Long deviceId, String deviceName, Instant startDate, Instant endDate) {
        this();
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public Integer getOriginalCount() {
        return originalCount;
    }

    public void setOriginalCount(Integer originalCount) {
        this.originalCount = originalCount;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(Instant archivedDate) {
        this.archivedDate = archivedDate;
    }

    public byte[] getCompressedData() {
        return compressedData;
    }

    public void setCompressedData(byte[] compressedData) {
        this.compressedData = compressedData;
    }

    public Double getCompressionRatio() {
        return compressionRatio;
    }

    public void setCompressionRatio(Double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public ArchiveType getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(ArchiveType archiveType) {
        this.archiveType = archiveType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
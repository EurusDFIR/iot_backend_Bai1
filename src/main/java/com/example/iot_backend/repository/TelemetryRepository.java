package com.example.iot_backend.repository;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findByDeviceIdOrderByTsDesc(Long deviceId);

    // Tìm devices có dữ liệu cũ hơn cutoff date
    @Query("SELECT DISTINCT t.device FROM Telemetry t WHERE t.ts < :cutoffDate")
    List<Device> findDevicesWithDataBeforeDate(@Param("cutoffDate") Instant cutoffDate);

    // Tìm các ngày khác nhau có dữ liệu cũ hơn cutoff date cho device
    @Query("SELECT DISTINCT DATE_TRUNC('day', t.ts) FROM Telemetry t " +
            "WHERE t.device.id = :deviceId AND t.ts < :cutoffDate ORDER BY DATE_TRUNC('day', t.ts)")
    List<Instant> findDistinctDatesBeforeCutoff(@Param("deviceId") Long deviceId,
            @Param("cutoffDate") Instant cutoffDate);

    // Tìm telemetry theo device và khoảng thời gian
    @Query("SELECT t FROM Telemetry t WHERE t.device = :device AND t.ts >= :startDate AND t.ts < :endDate ORDER BY t.ts")
    List<Telemetry> findByDeviceAndTimestampBetween(@Param("device") Device device,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Xóa telemetry theo device và khoảng thời gian
    @Modifying
    @Query("DELETE FROM Telemetry t WHERE t.device = :device AND t.ts >= :startDate AND t.ts < :endDate")
    void deleteByDeviceAndTimestampBetween(@Param("device") Device device,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Đếm số lượng records theo device và khoảng thời gian
    @Query("SELECT COUNT(t) FROM Telemetry t WHERE t.device.id = :deviceId AND t.ts >= :startDate AND t.ts < :endDate")
    long countByDeviceAndTimestampBetween(@Param("deviceId") Long deviceId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Tìm telemetry cũ nhất
    @Query("SELECT MIN(t.ts) FROM Telemetry t WHERE t.device.id = :deviceId")
    Instant findOldestTimestamp(@Param("deviceId") Long deviceId);

    // Tìm telemetry mới nhất
    @Query("SELECT MAX(t.ts) FROM Telemetry t WHERE t.device.id = :deviceId")
    Instant findLatestTimestamp(@Param("deviceId") Long deviceId);
}
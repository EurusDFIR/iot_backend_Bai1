package com.example.iot_backend.repository;

import com.example.iot_backend.model.DeviceStatus;
import com.example.iot_backend.model.DeviceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {

    // Tìm status theo device ID
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.device.id = :deviceId")
    Optional<DeviceStatus> findByDeviceId(@Param("deviceId") Long deviceId);

    // Tìm tất cả devices theo status
    List<DeviceStatus> findByStatus(DeviceStatusEnum status);

    // Tìm devices theo status và sắp xếp theo lastSeen
    List<DeviceStatus> findByStatusOrderByLastSeenDesc(DeviceStatusEnum status);

    // Tìm devices offline quá lâu
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.status = 'ONLINE' AND ds.lastHeartbeat < :timeoutThreshold")
    List<DeviceStatus> findTimedOutDevices(@Param("timeoutThreshold") Instant timeoutThreshold);

    // Tìm devices với battery thấp
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.batteryLevel IS NOT NULL AND ds.batteryLevel < :threshold AND ds.status = 'ONLINE'")
    List<DeviceStatus> findLowBatteryDevices(@Param("threshold") Integer threshold);

    // Tìm devices với signal yếu
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.signalStrength IS NOT NULL AND ds.signalStrength < :threshold AND ds.status = 'ONLINE'")
    List<DeviceStatus> findWeakSignalDevices(@Param("threshold") Integer threshold);

    // Thống kê devices theo status
    @Query("SELECT ds.status, COUNT(ds) FROM DeviceStatus ds GROUP BY ds.status")
    List<Object[]> getDeviceStatusCounts();

    // Tìm top devices theo uptime
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.totalUptimeSeconds > 0 ORDER BY ds.totalUptimeSeconds DESC")
    List<DeviceStatus> findTopUptimeDevices();

    // Tìm devices mới kết nối gần đây
    @Query("SELECT ds FROM DeviceStatus ds WHERE ds.lastConnectTime > :since ORDER BY ds.lastConnectTime DESC")
    List<DeviceStatus> findRecentlyConnectedDevices(@Param("since") Instant since);

    // Kiểm tra device có tồn tại status không
    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DeviceStatus ds WHERE ds.device.id = :deviceId")
    boolean existsByDeviceId(@Param("deviceId") Long deviceId);
}
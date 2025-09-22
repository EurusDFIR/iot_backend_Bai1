package com.example.iot_backend.repository;

import com.example.iot_backend.model.TelemetryArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryArchiveRepository extends JpaRepository<TelemetryArchive, Long> {

    // Tìm archive theo device và khoảng thời gian
    @Query("SELECT ta FROM TelemetryArchive ta WHERE ta.deviceId = :deviceId " +
            "AND ta.startDate <= :endDate AND ta.endDate >= :startDate")
    List<TelemetryArchive> findByDeviceIdAndDateRange(@Param("deviceId") Long deviceId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Tìm archive theo device
    List<TelemetryArchive> findByDeviceIdOrderByStartDateDesc(Long deviceId);

    // Tìm archive theo archive type
    List<TelemetryArchive> findByArchiveTypeOrderByArchivedDateDesc(TelemetryArchive.ArchiveType archiveType);

    // Tìm archive cũ để cleanup
    @Query("SELECT ta FROM TelemetryArchive ta WHERE ta.archivedDate < :cutoffDate")
    List<TelemetryArchive> findOldArchives(@Param("cutoffDate") Instant cutoffDate);

    // Thống kê dung lượng archive theo device
    @Query("SELECT ta.deviceId, ta.deviceName, COUNT(ta), SUM(ta.originalCount), AVG(ta.compressionRatio) " +
            "FROM TelemetryArchive ta GROUP BY ta.deviceId, ta.deviceName")
    List<Object[]> getArchiveStatsByDevice();

    // Thống kê tổng dung lượng archive
    @Query("SELECT COUNT(ta), SUM(ta.originalCount), AVG(ta.compressionRatio) FROM TelemetryArchive ta")
    Object[] getTotalArchiveStats();

    // Kiểm tra xem khoảng thời gian đã được archive chưa
    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TelemetryArchive ta " +
            "WHERE ta.deviceId = :deviceId AND ta.startDate <= :startDate AND ta.endDate >= :endDate")
    boolean isDateRangeArchived(@Param("deviceId") Long deviceId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Tìm archive theo tháng
    @Query("SELECT ta FROM TelemetryArchive ta WHERE YEAR(ta.startDate) = :year AND MONTH(ta.startDate) = :month")
    List<TelemetryArchive> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Xóa archive cũ hơn ngày cutoff
    @Query("DELETE FROM TelemetryArchive ta WHERE ta.archivedDate < :cutoffDate")
    void deleteOldArchives(@Param("cutoffDate") Instant cutoffDate);
}
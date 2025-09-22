package com.example.iot_backend.service;

import com.example.iot_backend.dto.TelemetryArchiveDTO;
import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.model.TelemetryArchive;
import com.example.iot_backend.repository.DeviceRepository;
import com.example.iot_backend.repository.TelemetryArchiveRepository;
import com.example.iot_backend.repository.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelemetryArchiveService {
    private static final Logger log = LoggerFactory.getLogger(TelemetryArchiveService.class);

    // Archive settings
    private static final int ARCHIVE_DAYS_THRESHOLD = 30; // Archive data older than 30 days
    private static final int DELETE_DAYS_THRESHOLD = 365; // Delete original data after 1 year
    private static final int BATCH_SIZE = 1000; // Process 1000 records per batch
    private static final int MAX_ARCHIVE_DAYS = 730; // Keep archives for 2 years

    private final TelemetryRepository telemetryRepo;
    private final TelemetryArchiveRepository archiveRepo;
    private final DeviceRepository deviceRepo;
    private final DataCompressionService compressionService;

    public TelemetryArchiveService(TelemetryRepository telemetryRepo,
            TelemetryArchiveRepository archiveRepo,
            DeviceRepository deviceRepo,
            DataCompressionService compressionService) {
        this.telemetryRepo = telemetryRepo;
        this.archiveRepo = archiveRepo;
        this.deviceRepo = deviceRepo;
        this.compressionService = compressionService;
    }

    /**
     * Scheduled task chạy hàng ngày để archive dữ liệu cũ
     * Chạy lúc 2:00 AM mỗi ngày
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Async
    public void scheduledArchiveOldData() {
        log.info("Starting scheduled telemetry archiving...");

        try {
            Instant cutoffDate = Instant.now().minus(ARCHIVE_DAYS_THRESHOLD, ChronoUnit.DAYS);
            archiveOldTelemetryData(cutoffDate);

            log.info("Scheduled telemetry archiving completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled archiving: {}", e.getMessage(), e);
        }
    }

    /**
     * Archive dữ liệu telemetry cũ hơn cutoff date
     */
    @Transactional
    public void archiveOldTelemetryData(Instant cutoffDate) {
        log.info("Archiving telemetry data older than: {}", cutoffDate);

        // Lấy danh sách devices có dữ liệu cần archive
        List<Device> devicesWithOldData = telemetryRepo.findDevicesWithDataBeforeDate(cutoffDate);

        for (Device device : devicesWithOldData) {
            try {
                archiveDeviceTelemetryData(device, cutoffDate);
            } catch (Exception e) {
                log.error("Error archiving data for device {}: {}", device.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Archive dữ liệu telemetry của một device cụ thể
     */
    @Transactional
    public void archiveDeviceTelemetryData(Device device, Instant cutoffDate) {
        log.info("Archiving telemetry data for device: {} ({})", device.getId(), device.getName());

        // Phân chia dữ liệu theo ngày để archive
        List<Instant> datesToArchive = telemetryRepo.findDistinctDatesBeforeCutoff(device.getId(), cutoffDate);

        for (Instant date : datesToArchive) {
            try {
                archiveDailyData(device, date);
            } catch (Exception e) {
                log.error("Error archiving daily data for device {} on date {}: {}",
                        device.getId(), date, e.getMessage(), e);
            }
        }
    }

    /**
     * Archive dữ liệu của một ngày cụ thể
     */
    @Transactional
    public void archiveDailyData(Device device, Instant date) {
        Instant startOfDay = date.truncatedTo(ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        // Kiểm tra xem đã archive chưa
        boolean alreadyArchived = archiveRepo.isDateRangeArchived(device.getId(), startOfDay, endOfDay);
        if (alreadyArchived) {
            log.debug("Data for device {} on {} already archived", device.getId(), date);
            return;
        }

        // Lấy dữ liệu telemetry trong ngày
        List<Telemetry> dailyData = telemetryRepo.findByDeviceAndTimestampBetween(
                device, startOfDay, endOfDay);

        if (dailyData.isEmpty()) {
            log.debug("No data to archive for device {} on {}", device.getId(), date);
            return;
        }

        try {
            // Chuyển đổi Telemetry entities thành DTOs để avoid Jackson serialization
            // issues
            List<TelemetryArchiveDTO> dtoList = dailyData.stream()
                    .map(t -> new TelemetryArchiveDTO(
                            t.getId(),
                            t.getDevice().getId(),
                            t.getDevice().getName(),
                            t.getTs(),
                            t.getData()))
                    .collect(Collectors.toList());

            // Nén dữ liệu DTO
            byte[] compressedData = compressionService.compressData(dtoList);

            // Tính toán thống kê
            int originalSize = estimateOriginalSize(dailyData);
            double compressionRatio = compressionService.calculateCompressionRatio(originalSize, compressedData.length);

            // Tạo archive record
            TelemetryArchive archive = new TelemetryArchive(device.getId(), device.getName(), startOfDay, endOfDay);
            archive.setOriginalCount(dailyData.size());
            archive.setCompressedData(compressedData);
            archive.setCompressionRatio(compressionRatio);
            archive.setArchiveType(TelemetryArchive.ArchiveType.DAILY);

            // Lưu archive
            archiveRepo.save(archive);

            // Xóa dữ liệu gốc nếu quá cũ
            Instant deleteCutoff = Instant.now().minus(DELETE_DAYS_THRESHOLD, ChronoUnit.DAYS);
            if (endOfDay.isBefore(deleteCutoff)) {
                telemetryRepo.deleteByDeviceAndTimestampBetween(device, startOfDay, endOfDay);
                log.info("Deleted {} original telemetry records for device {} on {}",
                        dailyData.size(), device.getId(), date);
            }

            log.info("Archived {} telemetry records for device {} on {} (compression ratio: {:.2f})",
                    dailyData.size(), device.getId(), date, compressionRatio);

        } catch (Exception e) {
            log.error("Error creating archive for device {} on {}: {}",
                    device.getId(), date, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieve archived data cho một device và khoảng thời gian
     */
    public List<Telemetry> retrieveArchivedData(Long deviceId, Instant startDate, Instant endDate) {
        log.info("Retrieving archived data for device {} from {} to {}", deviceId, startDate, endDate);

        List<TelemetryArchive> archives = archiveRepo.findByDeviceIdAndDateRange(deviceId, startDate, endDate);

        return archives.stream()
                .flatMap(archive -> {
                    try {
                        // Decompress to DTO list first
                        List<TelemetryArchiveDTO> dtoList = compressionService.decompressToList(
                                archive.getCompressedData(), TelemetryArchiveDTO.class);

                        // Convert DTOs back to Telemetry entities
                        return dtoList.stream().map(dto -> {
                            Telemetry telemetry = new Telemetry();
                            telemetry.setId(dto.getId());
                            telemetry.setTs(dto.getTs());
                            telemetry.setData(dto.getData());

                            // Set device (need to fetch from repo or create minimal object)
                            Device device = new Device();
                            device.setId(dto.getDeviceId());
                            device.setName(dto.getDeviceName());
                            telemetry.setDevice(device);

                            return telemetry;
                        });
                    } catch (Exception e) {
                        log.error("Error decompressing archive {}: {}", archive.getId(), e.getMessage());
                        return List.<Telemetry>of().stream();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy thống kê archive
     */
    public Map<String, Object> getArchiveStatistics() {
        try {
            Object[] totalStats = archiveRepo.getTotalArchiveStats();
            List<Object[]> deviceStats = archiveRepo.getArchiveStatsByDevice();

            // Handle null values from queries when no data exists
            Long totalArchives = totalStats != null && totalStats[0] != null ? ((Number) totalStats[0]).longValue()
                    : 0L;
            Long totalRecordsArchived = totalStats != null && totalStats[1] != null
                    ? ((Number) totalStats[1]).longValue()
                    : 0L;
            Double averageCompressionRatio = totalStats != null && totalStats[2] != null
                    ? ((Number) totalStats[2]).doubleValue()
                    : 0.0;

            return Map.of(
                    "totalArchives", totalArchives,
                    "totalRecordsArchived", totalRecordsArchived,
                    "averageCompressionRatio", averageCompressionRatio,
                    "deviceStatistics", deviceStats != null ? deviceStats : List.of());
        } catch (Exception e) {
            log.error("Error getting archive statistics: {}", e.getMessage(), e);
            // Return empty statistics on error
            return Map.of(
                    "totalArchives", 0L,
                    "totalRecordsArchived", 0L,
                    "averageCompressionRatio", 0.0,
                    "deviceStatistics", List.of());
        }
    }

    /**
     * Cleanup archives cũ
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Chạy chủ nhật hàng tuần lúc 3:00 AM
    @Async
    public void cleanupOldArchives() {
        log.info("Starting cleanup of old archives...");

        try {
            Instant cutoffDate = Instant.now().minus(MAX_ARCHIVE_DAYS, ChronoUnit.DAYS);
            List<TelemetryArchive> oldArchives = archiveRepo.findOldArchives(cutoffDate);

            for (TelemetryArchive archive : oldArchives) {
                archiveRepo.delete(archive);
                log.debug("Deleted old archive: {} (archived on {})", archive.getId(), archive.getArchivedDate());
            }

            log.info("Cleanup completed. Deleted {} old archives", oldArchives.size());
        } catch (Exception e) {
            log.error("Error during archive cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Ước tính kích thước dữ liệu gốc
     */
    private int estimateOriginalSize(List<Telemetry> data) {
        if (data.isEmpty())
            return 0;

        // Ước tính dựa trên record đầu tiên
        try {
            long sampleSize = compressionService.estimateCompressedSize(data.get(0));
            return (int) (sampleSize * data.size());
        } catch (Exception e) {
            // Fallback estimation
            return data.size() * 500; // Ước tính 500 bytes/record
        }
    }

    /**
     * Force archive data cho testing
     */
    public void forceArchiveData(Long deviceId, Instant startDate, Instant endDate) {
        Device device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        // Archive theo ngày
        Instant current = startDate.truncatedTo(ChronoUnit.DAYS);
        Instant end = endDate.truncatedTo(ChronoUnit.DAYS);

        while (!current.isAfter(end)) {
            archiveDailyData(device, current);
            current = current.plus(1, ChronoUnit.DAYS);
        }
    }
}
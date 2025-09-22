package com.example.iot_backend.controller;

import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.service.TelemetryArchiveService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-optimization")
public class DataOptimizationController {

    private final TelemetryArchiveService archiveService;

    public DataOptimizationController(TelemetryArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    /**
     * Lấy thống kê archive
     */
    @GetMapping("/archive/statistics")
    public ResponseEntity<Map<String, Object>> getArchiveStatistics() {
        Map<String, Object> stats = archiveService.getArchiveStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Retrieve archived data cho một device và khoảng thời gian
     */
    @GetMapping("/archive/data")
    public ResponseEntity<List<Telemetry>> getArchivedData(
            @RequestParam Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        List<Telemetry> data = archiveService.retrieveArchivedData(deviceId, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    /**
     * Force archive data cho một device (for testing/manual operations)
     */
    @PostMapping("/archive/force")
    public ResponseEntity<String> forceArchiveData(
            @RequestParam Long deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        try {
            // Default to last 7 days if dates not provided
            if (startDate == null) {
                startDate = Instant.now().minus(7, ChronoUnit.DAYS);
            }
            if (endDate == null) {
                endDate = Instant.now().minus(1, ChronoUnit.DAYS);
            }

            archiveService.forceArchiveData(deviceId, startDate, endDate);

            return ResponseEntity.ok("Archive process initiated for device " + deviceId +
                    " from " + startDate + " to " + endDate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Archive dữ liệu cũ hơn số ngày chỉ định
     */
    @PostMapping("/archive/old-data")
    public ResponseEntity<String> archiveOldData(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            Instant cutoffDate = Instant.now().minus(daysOld, ChronoUnit.DAYS);
            archiveService.archiveOldTelemetryData(cutoffDate);

            return ResponseEntity.ok("Archive process initiated for data older than " + daysOld + " days");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Cleanup archives cũ
     */
    @PostMapping("/archive/cleanup")
    public ResponseEntity<String> cleanupOldArchives() {
        try {
            archiveService.cleanupOldArchives();
            return ResponseEntity.ok("Archive cleanup process initiated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Lấy storage optimization recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
        Map<String, Object> stats = archiveService.getArchiveStatistics();

        // Tạo recommendations dựa trên statistics
        Map<String, Object> recommendations = Map.of(
                "totalArchives", stats.get("totalArchives"),
                "totalRecordsArchived", stats.get("totalRecordsArchived"),
                "averageCompressionRatio", stats.get("averageCompressionRatio"),
                "recommendations", List.of(
                        "Consider archiving data older than 30 days",
                        "Monitor compression ratios to ensure efficiency",
                        "Schedule regular cleanup of old archives",
                        "Implement data retention policies based on device importance"));

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get storage usage overview
     */
    @GetMapping("/storage/usage")
    public ResponseEntity<Map<String, Object>> getStorageUsage() {
        Map<String, Object> stats = archiveService.getArchiveStatistics();

        // Calculate storage metrics
        long totalRecords = stats.get("totalRecordsArchived") != null
                ? ((Number) stats.get("totalRecordsArchived")).longValue()
                : 0;
        double avgCompressionRatio = stats.get("averageCompressionRatio") != null
                ? ((Number) stats.get("averageCompressionRatio")).doubleValue()
                : 0.0;

        // Estimate original size vs compressed size
        long estimatedOriginalSize = totalRecords * 500; // 500 bytes per record estimate
        long estimatedCompressedSize = (long) (estimatedOriginalSize * avgCompressionRatio);
        long spaceSaved = estimatedOriginalSize - estimatedCompressedSize;

        Map<String, Object> usage = Map.of(
                "totalRecordsArchived", totalRecords,
                "estimatedOriginalSizeBytes", estimatedOriginalSize,
                "estimatedCompressedSizeBytes", estimatedCompressedSize,
                "spaceSavedBytes", spaceSaved,
                "spaceSavedPercentage", totalRecords > 0 ? (double) spaceSaved / estimatedOriginalSize * 100 : 0,
                "averageCompressionRatio", avgCompressionRatio);

        return ResponseEntity.ok(usage);
    }
}
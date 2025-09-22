# Lab 7 - Data Storage Optimization Testing Guide

## Mô tả

Lab 7 triển khai hệ thống tối ưu hóa lưu trữ dữ liệu với nén GZIP và archive telemetry cũ.

## API Endpoints của Lab 7

### 1. **Archive Statistics API**

- **URL**: `GET /api/data-optimization/archive/statistics`
- **Mô tả**: Lấy thống kê về archive data

**Postman Test:**

```
Method: GET
URL: http://localhost:8080/api/data-optimization/archive/statistics
Headers: Content-Type: application/json
```

**Expected Response:**

```json
{
  "totalArchives": 0,
  "totalRecordsArchived": 0,
  "averageCompressionRatio": 0.0,
  "deviceStatistics": []
}
```

### 2. **Storage Usage API**

- **URL**: `GET /api/data-optimization/storage/usage`
- **Mô tả**: Lấy thông tin sử dụng storage

**Postman Test:**

```
Method: GET
URL: http://localhost:8080/api/data-optimization/storage/usage
Headers: Content-Type: application/json
```

**Expected Response:**

```json
{
  "totalRecordsArchived": 0,
  "estimatedOriginalSizeBytes": 0,
  "estimatedCompressedSizeBytes": 0,
  "spaceSavedBytes": 0,
  "spaceSavedPercentage": 0.0,
  "averageCompressionRatio": 0.0
}
```

### 3. **Optimization Recommendations API**

- **URL**: `GET /api/data-optimization/recommendations`
- **Mô tả**: Lấy gợi ý tối ưu hóa storage

**Postman Test:**

```
Method: GET
URL: http://localhost:8080/api/data-optimization/recommendations
Headers: Content-Type: application/json
```

**Expected Response:**

```json
{
  "totalArchives": 0,
  "totalRecordsArchived": 0,
  "averageCompressionRatio": 0.0,
  "recommendations": [
    "Consider archiving data older than 30 days",
    "Monitor compression ratios to ensure efficiency",
    "Schedule regular cleanup of old archives",
    "Implement data retention policies based on device importance"
  ]
}
```

### 4. **Force Archive Data API**

- **URL**: `POST /api/data-optimization/archive/force`
- **Mô tả**: Force archive data cho một device cụ thể

**Postman Test:**

```
Method: POST
URL: http://localhost:8080/api/data-optimization/archive/force?deviceId=1
Headers: Content-Type: application/json
```

**Parameters:**

- `deviceId` (required): ID của device
- `startDate` (optional): Ngày bắt đầu (ISO format)
- `endDate` (optional): Ngày kết thúc (ISO format)

**Expected Response:**

```
Archive process initiated for device 1 from 2025-09-15T17:27:35.569Z to 2025-09-21T17:27:35.569Z
```

### 5. **Archive Old Data API**

- **URL**: `POST /api/data-optimization/archive/old-data`
- **Mô tả**: Archive dữ liệu cũ hơn số ngày chỉ định

**Postman Test:**

```
Method: POST
URL: http://localhost:8080/api/data-optimization/archive/old-data?daysOld=30
Headers: Content-Type: application/json
```

**Parameters:**

- `daysOld` (optional, default=30): Số ngày để xác định dữ liệu cũ

**Expected Response:**

```
Archive process initiated for data older than 30 days
```

### 6. **Archive Cleanup API**

- **URL**: `POST /api/data-optimization/archive/cleanup`
- **Mô tả**: Cleanup archives cũ

**Postman Test:**

```
Method: POST
URL: http://localhost:8080/api/data-optimization/archive/cleanup
Headers: Content-Type: application/json
```

**Expected Response:**

```
Archive cleanup process initiated
```

### 7. **Get Archived Data API**

- **URL**: `GET /api/data-optimization/archive/data`
- **Mô tả**: Lấy dữ liệu đã archive cho một device

**Postman Test:**

```
Method: GET
URL: http://localhost:8080/api/data-optimization/archive/data?deviceId=1&startDate=2025-09-15T00:00:00Z&endDate=2025-09-22T23:59:59Z
Headers: Content-Type: application/json
```

**Parameters:**

- `deviceId` (required): ID của device
- `startDate` (required): Ngày bắt đầu (ISO format)
- `endDate` (required): Ngày kết thúc (ISO format)

**Expected Response:**

```json
[]
```

## Test Scenarios

### Scenario 1: Basic API Testing

1. Test tất cả GET APIs để xem có trả về 200 OK không
2. Kiểm tra format response có đúng JSON schema không
3. Xác nhận initial values là 0 vì chưa có data

### Scenario 2: Archive Process Testing

1. Tạo device và telemetry data trước (dùng APIs từ Lab trước)
2. Test Force Archive API với deviceId hợp lệ
3. Kiểm tra statistics sau khi archive
4. Test retrieve archived data

### Scenario 3: Error Handling Testing

1. Test với deviceId không tồn tại
2. Test với tham số invalid (âm, string)
3. Test với date format sai

## Database Schema

### TelemetryArchive Table

```sql
CREATE TABLE telemetry_archive (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    device_name VARCHAR(255),
    archive_type VARCHAR(255) CHECK (archive_type IN ('HOURLY','DAILY','WEEKLY','MONTHLY')),
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    archived_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    original_count INTEGER NOT NULL,
    compression_ratio DOUBLE PRECISION,
    compressed_data BYTEA
);
```

## Features Implemented

### 1. Data Compression Service

- **Class**: `DataCompressionService`
- **Function**: Nén/giải nén dữ liệu telemetry bằng GZIP
- **Features**:
  - compressData(): Nén list telemetry thành byte array
  - decompressToString(): Giải nén thành JSON string
  - calculateCompressionRatio(): Tính tỷ lệ nén

### 2. Telemetry Archive Service

- **Class**: `TelemetryArchiveService`
- **Function**: Quản lý việc archive dữ liệu cũ
- **Features**:
  - Scheduled task archive dữ liệu hàng ngày
  - Force archive manual
  - Retrieve archived data với giải nén
  - Statistics và cleanup

### 3. Data Optimization Controller

- **Class**: `DataOptimizationController`
- **Function**: REST APIs cho data optimization
- **Features**:
  - Statistics APIs
  - Archive management APIs
  - Storage usage overview
  - Optimization recommendations

## Monitoring & Logs

Kiểm tra logs để thấy:

- Archive processes running
- Compression ratios achieved
- Database queries executed
- Error handling

## Notes

1. **Initial State**: Khi mới setup, tất cả statistics sẽ là 0 vì chưa có archived data
2. **Data Generation**: Cần tạo telemetry data trước để test archive functions
3. **Scheduled Tasks**: Archive tự động chạy hàng ngày lúc 2:00 AM
4. **Error Handling**: Tất cả APIs đều có exception handling trả về appropriate error messages
5. **PostgreSQL Compatibility**: Database schema đã được optimized cho PostgreSQL

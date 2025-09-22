-- Lab 7: Database Optimization Indexes
-- Thêm indexes để tối ưu performance cho archive và telemetry queries

-- ==== Telemetry Table Indexes ====

-- Index chính cho queries theo device và timestamp (archive operations)
CREATE INDEX IF NOT EXISTS idx_telemetry_device_timestamp 
ON telemetry (device_id, ts DESC);

-- Index cho archive queries theo timestamp
CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp 
ON telemetry (ts);

-- Index cho queries theo device
CREATE INDEX IF NOT EXISTS idx_telemetry_device 
ON telemetry (device_id);

-- Composite index cho date range queries
CREATE INDEX IF NOT EXISTS idx_telemetry_device_date_range 
ON telemetry (device_id, ts) WHERE ts IS NOT NULL;

-- Partial index cho dữ liệu gần đây (last 30 days)
CREATE INDEX IF NOT EXISTS idx_telemetry_recent 
ON telemetry (device_id, ts) 
WHERE ts > NOW() - INTERVAL '30 days';

-- ==== Device Status Table Indexes ====

-- Index cho monitoring queries
CREATE INDEX IF NOT EXISTS idx_device_status_status 
ON device_status (status);

-- Index cho heartbeat monitoring
CREATE INDEX IF NOT EXISTS idx_device_status_heartbeat 
ON device_status (last_heartbeat) 
WHERE status = 'ONLINE';

-- Index cho battery monitoring
CREATE INDEX IF NOT EXISTS idx_device_status_battery 
ON device_status (battery_level) 
WHERE battery_level IS NOT NULL AND status = 'ONLINE';

-- Index cho signal strength monitoring
CREATE INDEX IF NOT EXISTS idx_device_status_signal 
ON device_status (signal_strength) 
WHERE signal_strength IS NOT NULL AND status = 'ONLINE';

-- Composite index cho status và last_seen
CREATE INDEX IF NOT EXISTS idx_device_status_status_lastseen 
ON device_status (status, last_seen DESC);

-- ==== Commands Table Indexes ====

-- Index cho command status queries
CREATE INDEX IF NOT EXISTS idx_commands_status 
ON commands (status);

-- Index cho device commands
CREATE INDEX IF NOT EXISTS idx_commands_device 
ON commands (device_id);

-- Composite index cho device và status
CREATE INDEX IF NOT EXISTS idx_commands_device_status 
ON commands (device_id, status);

-- Index cho timestamp queries
CREATE INDEX IF NOT EXISTS idx_commands_timestamp 
ON commands (created_at DESC);

-- ==== Archive Table Indexes (Already defined in entity) ====
-- These are automatically created by JPA annotations:
-- - idx_archived_telemetry_device_date
-- - idx_archived_telemetry_date

-- ==== Performance Monitoring Queries ====

-- Query để kiểm tra index usage
-- SELECT schemaname, tablename, indexname, idx_tup_read, idx_tup_fetch 
-- FROM pg_stat_user_indexes 
-- WHERE schemaname = 'public' 
-- ORDER BY idx_tup_read DESC;

-- Query để tìm slow queries
-- SELECT query, mean_time, calls, total_time
-- FROM pg_stat_statements 
-- WHERE mean_time > 100 
-- ORDER BY mean_time DESC;

-- ==== Table Statistics Update ====
-- Cập nhật statistics để query planner hoạt động tối ưu
ANALYZE telemetry;
ANALYZE device_status;
ANALYZE commands;
ANALYZE telemetry_archive;
ANALYZE devices;
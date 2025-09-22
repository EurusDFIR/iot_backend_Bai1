-- ================================
-- IoT Backend Database Initialization
-- ================================

-- Create database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS iotdb;

-- Create user nếu chưa tồn tại  
DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles WHERE rolname = 'iotuser'
   ) THEN
      CREATE USER iotuser WITH PASSWORD 'secret';
   END IF;
END
$$;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE iotdb TO iotuser;

-- Connect to iotdb
\c iotdb;

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO iotuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO iotuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO iotuser;

-- Create extension for UUID generation (optional)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Optimize PostgreSQL for IoT workload
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 100;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.7;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Reload configuration
SELECT pg_reload_conf();
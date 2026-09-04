-- Guardian AI Master Database Schema DDL (PostgreSQL + PostGIS)
-- Version: 0.1.0

-- Enable PostGIS geospatial extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. USERS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(30) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ==========================================
-- 2. GUARDIANS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS guardians (
    id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    guardian_name VARCHAR(100) NOT NULL,
    guardian_phone VARCHAR(30) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_guardians_user_id ON guardians(user_id);

-- ==========================================
-- 3. LOCATIONS TELEMETRY TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    location_point GEOMETRY(Point, 4326) NOT NULL,
    accuracy_meters REAL,
    speed_m_s REAL,
    battery_level INTEGER,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_locations_user_time ON locations(user_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_locations_gist ON locations USING GIST(location_point);

-- ==========================================
-- 4. INCIDENTS (SOS) TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS incidents (
    id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trigger_type VARCHAR(30) NOT NULL, -- 'MANUAL', 'SHAKE_DETECTED', 'FALL_DETECTED'
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CANCELLED', 'RESOLVED'
    location_point GEOMETRY(Point, 4326) NOT NULL,
    battery_level INTEGER,
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_incidents_user_id ON incidents(user_id);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_gist ON incidents USING GIST(location_point);

-- ==========================================
-- 5. CRIME DATA TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS crime_data (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    severity_score REAL NOT NULL DEFAULT 5.0,
    location_point GEOMETRY(Point, 4326) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_crime_gist ON crime_data USING GIST(location_point);
CREATE INDEX IF NOT EXISTS idx_crime_time ON crime_data(occurred_at DESC);

-- ==========================================
-- 6. SAFETY SCORES CACHE TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS safety_scores (
    id BIGSERIAL PRIMARY KEY,
    location_point GEOMETRY(Point, 4326) NOT NULL,
    radius_meters INTEGER NOT NULL DEFAULT 500,
    score REAL NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_safety_scores_gist ON safety_scores USING GIST(location_point);

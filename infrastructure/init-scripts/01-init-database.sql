-- RideStream Database Initialization Script
-- Creates initial database schema and extensions

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create database schema
CREATE SCHEMA IF NOT EXISTS ridestream;

-- Set search path
SET search_path TO ridestream, public;

-- Create ENUM types for status fields
CREATE TYPE driver_status AS ENUM ('AVAILABLE', 'BUSY', 'OFFLINE');
CREATE TYPE ride_status AS ENUM ('REQUESTED', 'MATCHED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Drivers table
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status driver_status DEFAULT 'OFFLINE',
    rating DECIMAL(3,2) DEFAULT 5.00,
    vehicle_make VARCHAR(50),
    vehicle_model VARCHAR(50),
    vehicle_year INTEGER,
    vehicle_license_plate VARCHAR(20),
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    last_location_update TIMESTAMP,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Riders table
CREATE TABLE IF NOT EXISTS riders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 5.00,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ride requests table
CREATE TABLE IF NOT EXISTS ride_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rider_id UUID NOT NULL REFERENCES riders(id),
    pickup_latitude DECIMAL(10,8) NOT NULL,
    pickup_longitude DECIMAL(11,8) NOT NULL,
    pickup_address VARCHAR(255),
    destination_latitude DECIMAL(10,8) NOT NULL,
    destination_longitude DECIMAL(11,8) NOT NULL,
    destination_address VARCHAR(255),
    status ride_status DEFAULT 'REQUESTED',
    estimated_fare DECIMAL(10,2),
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rides table (matched and ongoing rides)
CREATE TABLE IF NOT EXISTS rides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ride_request_id UUID UNIQUE NOT NULL REFERENCES ride_requests(id),
    driver_id UUID NOT NULL REFERENCES drivers(id),
    rider_id UUID NOT NULL REFERENCES riders(id),
    pickup_latitude DECIMAL(10,8) NOT NULL,
    pickup_longitude DECIMAL(11,8) NOT NULL,
    destination_latitude DECIMAL(10,8) NOT NULL,
    destination_longitude DECIMAL(11,8) NOT NULL,
    status ride_status DEFAULT 'MATCHED',
    estimated_arrival_minutes INTEGER,
    distance_km DECIMAL(10,2),
    estimated_fare DECIMAL(10,2),
    actual_fare DECIMAL(10,2),
    matched_at TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(255),
    driver_rating INTEGER CHECK (driver_rating >= 1 AND driver_rating <= 5),
    rider_rating INTEGER CHECK (rider_rating >= 1 AND rider_rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Driver location history table
CREATE TABLE IF NOT EXISTS driver_location_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID NOT NULL REFERENCES drivers(id),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    status driver_status NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_location ON drivers(current_latitude, current_longitude);
CREATE INDEX idx_drivers_last_update ON drivers(last_location_update);

CREATE INDEX idx_ride_requests_rider_id ON ride_requests(rider_id);
CREATE INDEX idx_ride_requests_status ON ride_requests(status);
CREATE INDEX idx_ride_requests_requested_at ON ride_requests(requested_at);

CREATE INDEX idx_rides_driver_id ON rides(driver_id);
CREATE INDEX idx_rides_rider_id ON rides(rider_id);
CREATE INDEX idx_rides_status ON rides(status);
CREATE INDEX idx_rides_ride_request_id ON rides(ride_request_id);

CREATE INDEX idx_location_history_driver_id ON driver_location_history(driver_id);
CREATE INDEX idx_location_history_recorded_at ON driver_location_history(recorded_at);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_drivers_updated_at BEFORE UPDATE ON drivers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_riders_updated_at BEFORE UPDATE ON riders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ride_requests_updated_at BEFORE UPDATE ON ride_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rides_updated_at BEFORE UPDATE ON rides
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing
INSERT INTO drivers (name, email, phone, status, vehicle_make, vehicle_model, vehicle_year, vehicle_license_plate) VALUES
    ('John Doe', 'john.doe@ridestream.com', '+1-555-0101', 'AVAILABLE', 'Toyota', 'Camry', 2022, 'ABC-1234'),
    ('Jane Smith', 'jane.smith@ridestream.com', '+1-555-0102', 'AVAILABLE', 'Honda', 'Accord', 2021, 'XYZ-5678'),
    ('Mike Johnson', 'mike.johnson@ridestream.com', '+1-555-0103', 'OFFLINE', 'Tesla', 'Model 3', 2023, 'TES-9012');

INSERT INTO riders (name, email, phone) VALUES
    ('Alice Brown', 'alice.brown@example.com', '+1-555-0201'),
    ('Bob Wilson', 'bob.wilson@example.com', '+1-555-0202'),
    ('Carol Davis', 'carol.davis@example.com', '+1-555-0203');

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ridestream TO ridestream;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ridestream TO ridestream;
GRANT USAGE ON SCHEMA ridestream TO ridestream;

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'RideStream database initialized successfully!';
    RAISE NOTICE 'Created tables: drivers, riders, ride_requests, rides, driver_location_history';
    RAISE NOTICE 'Created indexes for optimal query performance';
    RAISE NOTICE 'Inserted sample data for 3 drivers and 3 riders';
END $$;

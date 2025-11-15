package com.ridestream.matching.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a driver location update event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdate {

    private UUID driverId;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;
    private DriverStatus status;

    public LocationUpdate(UUID driverId, Double latitude, Double longitude, DriverStatus status) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.timestamp = Instant.now();
    }
}

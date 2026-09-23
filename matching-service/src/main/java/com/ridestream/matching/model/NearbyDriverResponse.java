package com.ridestream.matching.model;

import com.ridestream.shared.event.DriverStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for nearby driver queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriverResponse {

    private UUID driverId;
    private Double latitude;
    private Double longitude;
    private Double distanceInMeters;
    private DriverStatus status;

    public NearbyDriverResponse(UUID driverId, Double latitude, Double longitude, Double distanceInMeters) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceInMeters = distanceInMeters;
        this.status = DriverStatus.AVAILABLE; // Default for nearby queries
    }
}

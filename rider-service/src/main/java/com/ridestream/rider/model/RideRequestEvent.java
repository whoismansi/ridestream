package com.ridestream.rider.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka when a ride is requested
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestEvent {

    private UUID requestId;
    private UUID riderId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationAddress;
    private BigDecimal estimatedFare;
    private Instant requestedAt;
}

package com.ridestream.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Ride request, published to the {@code ride-requests} topic.
 *
 * <p>Produced by rider-service when a rider requests a ride, consumed by
 * matching-service to select a driver. Records are keyed by {@code requestId}.
 *
 * <p>{@code requestId} doubles as the idempotency key: consumers must treat a
 * repeated requestId as a duplicate delivery and not match a second driver.
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

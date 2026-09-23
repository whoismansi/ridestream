package com.ridestream.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Driver location update, published to the {@code driver-locations} topic.
 *
 * <p>Produced by driver-service, consumed by matching-service to maintain the
 * redis geo index. Records are keyed by {@code driverId} so all updates for a
 * given driver land on the same partition and preserve per-driver ordering.
 *
 * <p>{@code timestamp} is the event time, not the ingest time. Consumers must
 * compare it against the last-applied timestamp before overwriting state,
 * because kafka redelivery can surface an older update after a newer one.
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

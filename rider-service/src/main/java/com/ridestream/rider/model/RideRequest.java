package com.ridestream.rider.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * RideRequest entity representing a ride request made by a rider
 */
@Entity
@Table(name = "ride_requests", schema = "ridestream")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Rider ID is required")
    @Column(name = "rider_id", nullable = false)
    private UUID riderId;

    @NotNull(message = "Pickup latitude is required")
    @Column(name = "pickup_latitude", nullable = false, columnDefinition = "numeric(10,8)")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    @Column(name = "pickup_longitude", nullable = false, columnDefinition = "numeric(11,8)")
    private Double pickupLongitude;

    @Column(name = "pickup_address", length = 255)
    private String pickupAddress;

    @NotNull(message = "Destination latitude is required")
    @Column(name = "destination_latitude", nullable = false, columnDefinition = "numeric(10,8)")
    private Double destinationLatitude;

    @NotNull(message = "Destination longitude is required")
    @Column(name = "destination_longitude", nullable = false, columnDefinition = "numeric(11,8)")
    private Double destinationLongitude;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RideStatus status = RideStatus.REQUESTED;

    @Column(name = "estimated_fare", precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}

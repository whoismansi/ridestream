package com.ridestream.rider.repository;

import com.ridestream.rider.model.RideRequest;
import com.ridestream.rider.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing RideRequest entities
 */
@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, UUID> {

    /**
     * Find all ride requests for a specific rider
     * @param riderId the rider's ID
     * @return list of ride requests
     */
    List<RideRequest> findByRiderIdOrderByRequestedAtDesc(UUID riderId);

    /**
     * Find ride requests by status
     * @param status the ride status
     * @return list of ride requests
     */
    List<RideRequest> findByStatusOrderByRequestedAtDesc(RideStatus status);

    /**
     * Find ride requests for a rider with a specific status
     * @param riderId the rider's ID
     * @param status the ride status
     * @return list of ride requests
     */
    List<RideRequest> findByRiderIdAndStatus(UUID riderId, RideStatus status);
}

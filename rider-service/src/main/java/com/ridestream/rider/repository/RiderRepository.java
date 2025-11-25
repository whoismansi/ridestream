package com.ridestream.rider.repository;

import com.ridestream.rider.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Rider entities
 */
@Repository
public interface RiderRepository extends JpaRepository<Rider, UUID> {

    /**
     * Find a rider by email
     * @param email rider's email
     * @return Optional containing the rider if found
     */
    Optional<Rider> findByEmail(String email);

    /**
     * Check if a rider exists by email
     * @param email rider's email
     * @return true if rider exists
     */
    boolean existsByEmail(String email);
}

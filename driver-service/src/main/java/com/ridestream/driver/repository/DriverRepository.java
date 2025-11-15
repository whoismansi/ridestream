package com.ridestream.driver.repository;

import com.ridestream.driver.model.Driver;
import com.ridestream.driver.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Driver entity.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    // Find driver by email 
    Optional<Driver> findByEmail(String email);

    // Find all drivers by status
    List<Driver> findByStatus(DriverStatus status);

    // Check if email already exists
    boolean existsByEmail(String email);

}

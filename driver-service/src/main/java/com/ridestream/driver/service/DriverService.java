package com.ridestream.driver.service;

import com.ridestream.driver.kafka.LocationProducer;
import com.ridestream.driver.model.Driver;
import com.ridestream.shared.event.DriverStatus;
import com.ridestream.shared.event.LocationUpdate;
import com.ridestream.driver.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for Driver operations like driver registration, location updates, and status management
 */
@Service
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final LocationProducer locationProducer;

    public DriverService(DriverRepository driverRepository, LocationProducer locationProducer) {
        this.driverRepository = driverRepository;
        this.locationProducer = locationProducer;
    }

    /**
     * Register a new driver
     *
     * @param driver Driver object with registration details
     * @return Saved driver entity
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public Driver registerDriver(Driver driver) {
        logger.info("Registering new driver with email: {}", driver.getEmail());

        // Check if email already exists
        if (driverRepository.existsByEmail(driver.getEmail())) {
            logger.error("Driver registration failed - email already exists: {}", driver.getEmail());
            throw new IllegalArgumentException("Driver with email " + driver.getEmail() + " already exists");
        }

        // Set initial status to OFFLINE
        driver.setStatus(DriverStatus.OFFLINE);

        // Save to database
        Driver savedDriver = driverRepository.save(driver);
        logger.info("Driver registered successfully with ID: {}", savedDriver.getId());

        return savedDriver;
    }

    /**
     * Update driver location
     * Saves location to database and publishes to Kafka topic
     *
     * @param driverId Driver UUID
     * @param latitude New latitude
     * @param longitude New longitude
     * @return Updated driver entity
     * @throws IllegalArgumentException if driver not found
     */
    @Transactional
    public Driver updateLocation(UUID driverId, Double latitude, Double longitude) {
        logger.debug("Updating location for driver: {}", driverId);

        // Find driver
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        // Update driver location in database
        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        Driver updatedDriver = driverRepository.save(driver);

        // Create location update event
        LocationUpdate locationUpdate = new LocationUpdate(
            driverId,
            latitude,
            longitude,
            driver.getStatus()
        );

        // Publish to Kafka
        locationProducer.sendLocationUpdate(locationUpdate);

        logger.info("Location updated for driver: {} | Status: {}", driverId, driver.getStatus());

        return updatedDriver;
    }

    /**
     * Update driver status (AVAILABLE, BUSY, OFFLINE)
     * Also publishes location update to Kafka with new status
     *
     * @param driverId Driver UUID
     * @param status New status
     * @return Updated driver entity
     */
    @Transactional
    public Driver updateStatus(UUID driverId, DriverStatus status) {
        logger.info("Updating status for driver: {} to {}", driverId, status);

        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        driver.setStatus(status);
        Driver updatedDriver = driverRepository.save(driver);

        // Publish status change to Kafka (if driver has location)
        if (driver.getLatitude() != null && driver.getLongitude() != null) {
            LocationUpdate locationUpdate = new LocationUpdate(
                driverId,
                driver.getLatitude(),
                driver.getLongitude(),
                status
            );
            locationProducer.sendLocationUpdate(locationUpdate);
        }

        logger.info("Status updated for driver: {} | New status: {}", driverId, status);

        return updatedDriver;
    }

    /**
     * Get driver by ID
     *
     * @param driverId Driver UUID
     * @return Driver entity
     */
    public Driver getDriverById(UUID driverId) {
        logger.debug("Fetching driver with ID: {}", driverId);
        return driverRepository.findById(driverId)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));
    }

    /**
     * Get driver by email
     *
     * @param email Driver email
     * @return Driver entity
     */
    public Driver getDriverByEmail(String email) {
        logger.debug("Fetching driver with email: {}", email);
        return driverRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));
    }

    /**
     * Get all drivers by status
     *
     * @param status Driver status
     * @return List of drivers with the specified status
     */
    public List<Driver> getDriversByStatus(DriverStatus status) {
        logger.debug("Fetching drivers with status: {}", status);
        return driverRepository.findByStatus(status);
    }

    /**
     * Get all drivers
     *
     * @return List of all drivers
     */
    public List<Driver> getAllDrivers() {
        logger.debug("Fetching all drivers");
        return driverRepository.findAll();
    }

    /**
     * Delete driver
     *
     * @param driverId Driver UUID
     */
    @Transactional
    public void deleteDriver(UUID driverId) {
        logger.warn("Deleting driver with ID: {}", driverId);

        if (!driverRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found with ID: " + driverId);
        }

        driverRepository.deleteById(driverId);
        logger.info("Driver deleted: {}", driverId);
    }
}

package com.ridestream.driver.controller;

import com.ridestream.driver.model.Driver;
import com.ridestream.driver.model.DriverStatus;
import com.ridestream.driver.service.DriverService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Driver operations
 */
@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*") // Allow CORS
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    /**
     * Register a new driver
     * POST /api/drivers
     */
    @PostMapping
    public ResponseEntity<?> registerDriver(@Valid @RequestBody Driver driver) {
        try {
            logger.info("Received request to register driver: {}", driver.getEmail());
            Driver registeredDriver = driverService.registerDriver(driver);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredDriver);
        } catch (IllegalArgumentException e) {
            logger.error("Driver registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during driver registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Update driver location
     * PUT /api/drivers/{id}/location
     */
    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable UUID id,
            @RequestBody Map<String, Double> location) {
        try {
            Double latitude = location.get("latitude");
            Double longitude = location.get("longitude");

            if (latitude == null || longitude == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Both latitude and longitude are required"));
            }

            logger.info("Updating location for driver: {}", id);
            Driver updatedDriver = driverService.updateLocation(id, latitude, longitude);
            return ResponseEntity.ok(updatedDriver);
        } catch (IllegalArgumentException e) {
            logger.error("Location update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during location update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Update driver status
     * PUT /api/drivers/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String statusStr = statusUpdate.get("status");

            if (statusStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Status is required"));
            }

            DriverStatus status = DriverStatus.valueOf(statusStr.toUpperCase());
            logger.info("Updating status for driver: {} to {}", id, status);

            Driver updatedDriver = driverService.updateStatus(id, status);
            return ResponseEntity.ok(updatedDriver);
        } catch (IllegalArgumentException e) {
            logger.error("Status update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during status update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get driver by ID
     * GET /api/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDriverById(@PathVariable UUID id) {
        try {
            logger.debug("Fetching driver with ID: {}", id);
            Driver driver = driverService.getDriverById(id);
            return ResponseEntity.ok(driver);
        } catch (IllegalArgumentException e) {
            logger.error("Driver not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching driver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get all drivers
     * GET /api/drivers
     */
    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        logger.debug("Fetching all drivers");
        List<Driver> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by status
     * GET /api/drivers/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getDriversByStatus(@PathVariable String status) {
        try {
            DriverStatus driverStatus = DriverStatus.valueOf(status.toUpperCase());
            logger.debug("Fetching drivers with status: {}", driverStatus);

            List<Driver> drivers = driverService.getDriversByStatus(driverStatus);
            return ResponseEntity.ok(drivers);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid status. Valid values: AVAILABLE, BUSY, OFFLINE"));
        } catch (Exception e) {
            logger.error("Unexpected error fetching drivers by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Delete driver
     * DELETE /api/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDriver(@PathVariable UUID id) {
        try {
            logger.warn("Deleting driver with ID: {}", id);
            driverService.deleteDriver(id);
            return ResponseEntity.ok(Map.of("message", "Driver deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Driver deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during driver deletion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Health check endpoint
     * GET /api/drivers/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "driver-service"
        ));
    }
}

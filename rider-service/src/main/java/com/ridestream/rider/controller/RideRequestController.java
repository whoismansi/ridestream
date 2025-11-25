package com.ridestream.rider.controller;

import com.ridestream.rider.model.RideRequest;
import com.ridestream.rider.service.RideRequestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing ride requests
 */
@RestController
@RequestMapping("/api/ride-requests")
public class RideRequestController {

    private static final Logger logger = LoggerFactory.getLogger(RideRequestController.class);

    private final RideRequestService rideRequestService;

    public RideRequestController(RideRequestService rideRequestService) {
        this.rideRequestService = rideRequestService;
    }

    /**
     * Create a new ride request
     * POST /api/ride-requests
     */
    @PostMapping
    public ResponseEntity<?> createRideRequest(@Valid @RequestBody RideRequest rideRequest) {
        try {
            logger.info("Creating ride request for rider: {}", rideRequest.getRiderId());
            RideRequest created = rideRequestService.createRideRequest(rideRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create ride request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating ride request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create ride request: " + e.getMessage());
        }
    }

    /**
     * Get ride request by ID
     * GET /api/ride-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRideRequestById(@PathVariable UUID id) {
        try {
            logger.info("Fetching ride request with ID={}", id);
            RideRequest rideRequest = rideRequestService.getRideRequestById(id);
            return ResponseEntity.ok(rideRequest);
        } catch (IllegalArgumentException e) {
            logger.error("Ride request not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching ride request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch ride request: " + e.getMessage());
        }
    }

    /**
     * Get all ride requests for a rider
     * GET /api/ride-requests/rider/{riderId}
     */
    @GetMapping("/rider/{riderId}")
    public ResponseEntity<?> getRideRequestsByRiderId(@PathVariable UUID riderId) {
        try {
            logger.info("Fetching ride requests for rider", riderId);
            List<RideRequest> requests = rideRequestService.getRideRequestsByRiderId(riderId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error fetching ride requests for rider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch ride requests: " + e.getMessage());
        }
    }

    /**
     * Cancel a ride request
     * DELETE /api/ride-requests/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelRideRequest(@PathVariable UUID id) {
        try {
            logger.info("Cancelling ride request", id);
            RideRequest cancelled = rideRequestService.cancelRideRequest(id);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to cancel ride request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error cancelling ride request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to cancel ride request: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     * GET /api/ride-requests/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Ride Request Controller is healthy");
    }
}

package com.ridestream.rider.controller;

import com.ridestream.rider.model.Rider;
import com.ridestream.rider.service.RiderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing riders
 */
@RestController
@RequestMapping("/api/riders")
public class RiderController {

    private static final Logger logger = LoggerFactory.getLogger(RiderController.class);

    private final RiderService riderService;

    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }

    /**
     * Register a new rider
     * POST /api/riders
     */
    @PostMapping
    public ResponseEntity<?> registerRider(@Valid @RequestBody Rider rider) {
        try {
            logger.info("Registering new rider: email={}", rider.getEmail());
            Rider registered = riderService.registerRider(rider);
            return ResponseEntity.status(HttpStatus.CREATED).body(registered);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to register rider: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error registering rider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register rider: " + e.getMessage());
        }
    }

    /**
     * Get rider by ID
     * GET /api/riders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRiderById(@PathVariable UUID id) {
        try {
            logger.info("Fetching rider with ID={}", id);
            Rider rider = riderService.getRiderById(id);
            return ResponseEntity.ok(rider);
        } catch (IllegalArgumentException e) {
            logger.error("Rider not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching rider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch rider: " + e.getMessage());
        }
    }

    /**
     * Get all riders
     * GET /api/riders
     */
    @GetMapping
    public ResponseEntity<?> getAllRiders() {
        try {
            logger.info("Fetching all riders");
            List<Rider> riders = riderService.getAllRiders();
            return ResponseEntity.ok(riders);
        } catch (Exception e) {
            logger.error("Error fetching riders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch riders: " + e.getMessage());
        }
    }

    /**
     * Update rider information
     * PUT /api/riders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRider(@PathVariable UUID id, @Valid @RequestBody Rider rider) {
        try {
            logger.info("Updating rider with ID={}", id);
            Rider updated = riderService.updateRider(id, rider);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update rider: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating rider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update rider: " + e.getMessage());
        }
    }

    /**
     * Delete a rider
     * DELETE /api/riders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRider(@PathVariable UUID id) {
        try {
            logger.info("Deleting rider with ID={}", id);
            riderService.deleteRider(id);
            return ResponseEntity.ok("Rider deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete rider: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting rider", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete rider: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     * GET /api/riders/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Rider Controller is healthy");
    }
}

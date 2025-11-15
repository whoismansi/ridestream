package com.ridestream.matching.controller;

import com.ridestream.matching.model.NearbyDriverResponse;
import com.ridestream.matching.service.RedisLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for matching operations, finding nearby drivers
 */
@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*")
public class MatchingController {

    private static final Logger logger = LoggerFactory.getLogger(MatchingController.class);

    private final RedisLocationService redisLocationService;

    public MatchingController(RedisLocationService redisLocationService) {
        this.redisLocationService = redisLocationService;
    }

    /**
     * Find nearby available drivers
     *
     * @param latitude Center point latitude
     * @param longitude Center point longitude
     * @param radius Search radius in meters (default 5000m = 5km)
     * @return List of nearby drivers sorted by distance
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> findNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5000") Double radius) {

        logger.info("Finding drivers near ({}, {}) within {}m", latitude, longitude, radius);

        List<NearbyDriverResponse> nearbyDrivers =
            redisLocationService.findNearbyDrivers(latitude, longitude, radius);

        logger.info("Found {} available drivers", nearbyDrivers.size());

        return ResponseEntity.ok(nearbyDrivers);
    }

    /**
     * Get count of available drivers
     *
     * @return Number of available drivers in Redis
     */
    @GetMapping("/available-count")
    public ResponseEntity<Map<String, Long>> getAvailableDriverCount() {
        Long count = redisLocationService.getAvailableDriverCount();
        logger.debug("Available driver count: {}", count);
        return ResponseEntity.ok(Map.of("availableDrivers", count));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "matching-service"
        ));
    }
}

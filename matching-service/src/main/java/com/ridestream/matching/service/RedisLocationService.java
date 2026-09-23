package com.ridestream.matching.service;

import com.ridestream.shared.event.DriverStatus;
import com.ridestream.shared.event.LocationUpdate;
import com.ridestream.matching.model.NearbyDriverResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for storing and querying driver locations in Redis
 */
@Service
public class RedisLocationService {

    private static final Logger logger = LoggerFactory.getLogger(RedisLocationService.class);

    private static final String LOCATION_KEY = "drivers:locations";
    private static final String STATUS_KEY_PREFIX = "driver:status:";
    private static final long LOCATION_TTL_MINUTES = 5; // Expire after 5 minutes

    private final StringRedisTemplate redisTemplate;

    public RedisLocationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Store driver location in Redis using geospatial data structure
     *
     * @param locationUpdate Location update from Kafka
     */
    public void storeDriverLocation(LocationUpdate locationUpdate) {
        String driverId = locationUpdate.getDriverId().toString();

        try {
            // Only store AVAILABLE drivers in geospatial index
            if (locationUpdate.getStatus() == DriverStatus.AVAILABLE) {
                // Store location using GEOADD
                Point point = new Point(locationUpdate.getLongitude(), locationUpdate.getLatitude());
                redisTemplate.opsForGeo().add(LOCATION_KEY, point, driverId);

                // Store driver status separately with TTL
                String statusKey = STATUS_KEY_PREFIX + driverId;
                redisTemplate.opsForValue().set(statusKey, locationUpdate.getStatus().name());
                redisTemplate.expire(statusKey, LOCATION_TTL_MINUTES, TimeUnit.MINUTES);

                logger.info("Stored location for driver: {} at ({}, {})",
                    driverId, locationUpdate.getLatitude(), locationUpdate.getLongitude());
            } else {
                // Driver is BUSY or OFFLINE remove from available drivers
                removeDriverLocation(locationUpdate.getDriverId());
                logger.debug("Driver {} is {}, removed from available pool",
                    driverId, locationUpdate.getStatus());
            }
        } catch (Exception e) {
            logger.error("Failed to store location for driver: {}", driverId, e);
        }
    }

    /**
     * Find nearby available drivers within a radius
     *
     * @param latitude Center point latitude
     * @param longitude Center point longitude
     * @param radiusInMeters Search radius in meters
     * @return List of nearby drivers with distances
     */
    public List<NearbyDriverResponse> findNearbyDrivers(Double latitude, Double longitude, Double radiusInMeters) {
        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();

        try {
            Point center = new Point(longitude, latitude);
            Distance radius = new Distance(radiusInMeters / 1000.0, RedisGeoCommands.DistanceUnit.KILOMETERS);
            Circle searchArea = new Circle(center, radius);

            // GEORADIUS query with WITHDIST and WITHCOORD options
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(LOCATION_KEY, searchArea, args);

            if (results == null || results.getContent().isEmpty()) {
                logger.debug("No drivers found near ({}, {}) within {} meters",
                    latitude, longitude, radiusInMeters);
                return nearbyDrivers;
            }

            logger.info("Found {} drivers near ({}, {}) within {} meters",
                results.getContent().size(), latitude, longitude, radiusInMeters);

            // Convert results to response DTOs
            results.forEach(result -> {
                String driverIdStr = result.getContent().getName();
                UUID driverId = UUID.fromString(driverIdStr);

                // Get distance in meters
                Double distanceInMeters = result.getDistance().getValue() * 1000; // Convert km to meters

                // Get driver's exact location
                Point driverLocation = result.getContent().getPoint();

                if (driverLocation != null) {
                    NearbyDriverResponse response = new NearbyDriverResponse(
                        driverId,
                        driverLocation.getY(), // latitude
                        driverLocation.getX(), // longitude
                        distanceInMeters
                    );
                    nearbyDrivers.add(response);
                } else {
                    logger.warn("Driver {} has no location data in Redis", driverId);
                }
            });

        } catch (Exception e) {
            logger.error("Error finding nearby drivers", e);
        }

        return nearbyDrivers;
    }

    /**
     * Remove driver from available pool
     *
     * @param driverId Driver UUID
     */
    public void removeDriverLocation(UUID driverId) {
        String driverIdStr = driverId.toString();
        try {
            // Remove from geospatial index
            redisTemplate.opsForGeo().remove(LOCATION_KEY, driverIdStr);

            // Remove status
            String statusKey = STATUS_KEY_PREFIX + driverIdStr;
            redisTemplate.delete(statusKey);

            logger.debug("Removed driver {} from Redis", driverIdStr);
        } catch (Exception e) {
            logger.error("Failed to remove driver: {}", driverIdStr, e);
        }
    }

    /**
     * Get count of available drivers in Redis
     *
     * @return Number of available drivers
     */
    public Long getAvailableDriverCount() {
        try {
            // Geo data is stored in a sorted set
            return redisTemplate.opsForZSet().size(LOCATION_KEY);
        } catch (Exception e) {
            logger.error("Error getting driver count", e);
            return 0L;
        }
    }
}

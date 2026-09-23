package com.ridestream.matching.kafka;

import com.ridestream.shared.event.LocationUpdate;
import com.ridestream.matching.service.RedisLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for driver location updates
 */
@Component
public class LocationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LocationConsumer.class);

    private final RedisLocationService redisLocationService;

    public LocationConsumer(RedisLocationService redisLocationService) {
        this.redisLocationService = redisLocationService;
    }

    /**
     * Consume location updates from Kafka and store in Redis
     *
     * @param locationUpdate Location update event
     */
    @KafkaListener(
        topics = "${kafka.topic.driver-locations}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeLocationUpdate(LocationUpdate locationUpdate) {
        logger.info("Received location update for driver: {} | Status: {} | Location: ({}, {})",
            locationUpdate.getDriverId(),
            locationUpdate.getStatus(),
            locationUpdate.getLatitude(),
            locationUpdate.getLongitude());

        try {
            redisLocationService.storeDriverLocation(locationUpdate);

            logger.debug("Successfully processed location update for driver: {}",
                locationUpdate.getDriverId());

        } catch (Exception e) {
            logger.error("Failed to process location update for driver: {}",
                locationUpdate.getDriverId(), e);
        }
    }
}

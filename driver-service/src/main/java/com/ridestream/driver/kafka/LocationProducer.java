package com.ridestream.driver.kafka;

import com.ridestream.driver.model.LocationUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for publishing driver location updates
 * Sends LocationUpdate messages to the 'driver-locations' topic
 */
@Service
public class LocationProducer {

    private static final Logger logger = LoggerFactory.getLogger(LocationProducer.class);

    private final KafkaTemplate<String, LocationUpdate> kafkaTemplate;

    @Value("${kafka.topic.driver-locations}")
    private String topicName;

    public LocationProducer(KafkaTemplate<String, LocationUpdate> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send location update to Kafka topic
     *
     * @param locationUpdate The location update to send
     */
    public void sendLocationUpdate(LocationUpdate locationUpdate) {
        String key = locationUpdate.getDriverId().toString();

        logger.debug("Sending location update for driver: {} to topic: {}", key, topicName);

        CompletableFuture<SendResult<String, LocationUpdate>> future =
            kafkaTemplate.send(topicName, key, locationUpdate);

        // Handle success and failure callbacks
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Location update sent successfully for driver: {} | Partition: {} | Offset: {}",
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send location update for driver: {}", key, ex);
            }
        });
    }
}

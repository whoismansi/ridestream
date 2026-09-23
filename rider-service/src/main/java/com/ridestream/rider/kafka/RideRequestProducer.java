package com.ridestream.rider.kafka;

import com.ridestream.shared.event.RideRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing ride request events
 */
@Service
public class RideRequestProducer {

    private static final Logger logger = LoggerFactory.getLogger(RideRequestProducer.class);

    private final KafkaTemplate<String, RideRequestEvent> kafkaTemplate;

    @Value("${kafka.topics.ride-requests}")
    private String rideRequestsTopic;

    public RideRequestProducer(KafkaTemplate<String, RideRequestEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a ride request event to Kafka
     * @param event the ride request event to publish
     */
    public void publishRideRequest(RideRequestEvent event) {
        logger.info("Publishing ride request to Kafka: requestId={}, riderId={}, pickup=({}, {}), destination=({}, {})",
                event.getRequestId(), event.getRiderId(),
                event.getPickupLatitude(), event.getPickupLongitude(),
                event.getDestinationLatitude(), event.getDestinationLongitude());

        CompletableFuture<SendResult<String, RideRequestEvent>> future =
                kafkaTemplate.send(rideRequestsTopic, event.getRequestId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Successfully published ride request: requestId={}, topic={}, partition={}, offset={}",
                        event.getRequestId(), rideRequestsTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to publish ride request: requestId={}, error={}",
                        event.getRequestId(), ex.getMessage(), ex);
            }
        });
    }
}
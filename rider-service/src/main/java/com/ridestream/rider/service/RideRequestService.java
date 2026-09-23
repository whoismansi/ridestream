package com.ridestream.rider.service;

import com.ridestream.rider.kafka.RideRequestProducer;
import com.ridestream.rider.model.RideRequest;
import com.ridestream.shared.event.RideRequestEvent;
import com.ridestream.rider.model.RideStatus;
import com.ridestream.rider.repository.RideRequestRepository;
import com.ridestream.rider.repository.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing ride requests
 */
@Service
public class RideRequestService {

    private static final Logger logger = LoggerFactory.getLogger(RideRequestService.class);

    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;
    private final RideRequestProducer rideRequestProducer;

    // Pricing constants
    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(2.50);
    private static final BigDecimal PRICE_PER_KM = BigDecimal.valueOf(1.50);

    public RideRequestService(RideRequestRepository rideRequestRepository,
                              RiderRepository riderRepository,
                              RideRequestProducer rideRequestProducer) {
        this.rideRequestRepository = rideRequestRepository;
        this.riderRepository = riderRepository;
        this.rideRequestProducer = rideRequestProducer;
    }

    /**
     * Create a new ride request
     * @param rideRequest the ride request to create
     * @return the created ride request
     * @throws IllegalArgumentException if rider not found
     */
    @Transactional
    public RideRequest createRideRequest(RideRequest rideRequest) {
        logger.info("Creating ride request for rider: {}", rideRequest.getRiderId());

        // Validate rider exists
        if (!riderRepository.existsById(rideRequest.getRiderId())) {
            throw new IllegalArgumentException("Rider not found with id: " + rideRequest.getRiderId());
        }

        // Calculate estimated fare
        double distance = calculateDistance(
                rideRequest.getPickupLatitude(), rideRequest.getPickupLongitude(),
                rideRequest.getDestinationLatitude(), rideRequest.getDestinationLongitude()
        );
        BigDecimal estimatedFare = calculateFare(distance);
        rideRequest.setEstimatedFare(estimatedFare);

        // Save ride request
        RideRequest savedRequest = rideRequestRepository.save(rideRequest);
        logger.info("Ride request created: id={}, riderId={}, estimatedFare={}, distance={}km",
                savedRequest.getId(), savedRequest.getRiderId(), estimatedFare, distance);

        // Publish event to Kafka
        RideRequestEvent event = new RideRequestEvent(
                savedRequest.getId(),
                savedRequest.getRiderId(),
                savedRequest.getPickupLatitude(),
                savedRequest.getPickupLongitude(),
                savedRequest.getPickupAddress(),
                savedRequest.getDestinationLatitude(),
                savedRequest.getDestinationLongitude(),
                savedRequest.getDestinationAddress(),
                savedRequest.getEstimatedFare(),
                savedRequest.getRequestedAt()
        );
        rideRequestProducer.publishRideRequest(event);

        return savedRequest;
    }

    /**
     * Get ride request by ID
     * @param id ride request ID
     * @return the ride request
     * @throws IllegalArgumentException if not found
     */
    public RideRequest getRideRequestById(UUID id) {
        logger.debug("Fetching ride request by ID: {}", id);
        return rideRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ride request not found with id: " + id));
    }

    /**
     * Get all ride requests for a rider
     * @param riderId the rider's ID
     * @return list of ride requests
     */
    public List<RideRequest> getRideRequestsByRiderId(UUID riderId) {
        logger.debug("Fetching ride requests for rider: {}", riderId);
        return rideRequestRepository.findByRiderIdOrderByRequestedAtDesc(riderId);
    }

    /**
     * Cancel a ride request
     * @param id ride request ID
     * @return the cancelled ride request
     * @throws IllegalArgumentException if not found or already completed
     */
    @Transactional
    public RideRequest cancelRideRequest(UUID id) {
        logger.info("Cancelling ride request: {}", id);

        RideRequest request = getRideRequestById(id);

        if (request.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed ride");
        }

        if (request.getStatus() == RideStatus.CANCELLED) {
            logger.warn("Ride request already cancelled: {}", id);
            return request;
        }

        request.setStatus(RideStatus.CANCELLED);
        RideRequest saved = rideRequestRepository.save(request);
        logger.info("Ride request cancelled successfully: {}", id);

        return saved;
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 pickup latitude
     * @param lon1 pickup longitude
     * @param lat2 destination latitude
     * @param lon2 destination longitude
     * @return distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate fare based on distance
     * @param distanceKm distance in kilometers
     * @return estimated fare
     */
    private BigDecimal calculateFare(double distanceKm) {
        BigDecimal distanceFare = PRICE_PER_KM.multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal totalFare = BASE_FARE.add(distanceFare);
        return totalFare.setScale(2, RoundingMode.HALF_UP);
    }
}

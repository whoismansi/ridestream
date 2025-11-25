package com.ridestream.rider.service;

import com.ridestream.rider.model.Rider;
import com.ridestream.rider.repository.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing riders
 */
@Service
public class RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

    private final RiderRepository riderRepository;

    public RiderService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    /**
     * Register a new rider
     * @param rider the rider to register
     * @return the registered rider
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public Rider registerRider(Rider rider) {
        logger.info("Registering new rider: email={}", rider.getEmail());

        if (riderRepository.existsByEmail(rider.getEmail())) {
            logger.error("Rider registration failed: email already exists - {}", rider.getEmail());
            throw new IllegalArgumentException("Rider with email " + rider.getEmail() + " already exists");
        }

        Rider savedRider = riderRepository.save(rider);
        logger.info("Rider registered successfully: id={}, email={}", savedRider.getId(), savedRider.getEmail());
        return savedRider;
    }

    /**
     * Get rider by ID
     * @param id rider's ID
     * @return the rider
     * @throws IllegalArgumentException if rider not found
     */
    public Rider getRiderById(UUID id) {
        logger.debug("Fetching rider by ID: {}", id);
        return riderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rider not found with id: " + id));
    }

    /**
     * Get all riders
     * @return list of all riders
     */
    public List<Rider> getAllRiders() {
        logger.debug("Fetching all riders");
        return riderRepository.findAll();
    }

    /**
     * Update rider information
     * @param id rider's ID
     * @param updatedRider updated rider information
     * @return the updated rider
     * @throws IllegalArgumentException if rider not found
     */
    @Transactional
    public Rider updateRider(UUID id, Rider updatedRider) {
        logger.info("Updating rider: id={}", id);

        Rider existingRider = getRiderById(id);

        // Update fields
        if (updatedRider.getName() != null) {
            existingRider.setName(updatedRider.getName());
        }
        if (updatedRider.getPhone() != null) {
            existingRider.setPhone(updatedRider.getPhone());
        }
        // Email cannot be updated to prevent conflicts

        Rider saved = riderRepository.save(existingRider);
        logger.info("Rider updated successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * Delete a rider
     * @param id rider's ID
     * @throws IllegalArgumentException if rider not found
     */
    @Transactional
    public void deleteRider(UUID id) {
        logger.info("Deleting rider: id={}", id);

        if (!riderRepository.existsById(id)) {
            throw new IllegalArgumentException("Rider not found with id: " + id);
        }

        riderRepository.deleteById(id);
        logger.info("Rider deleted successfully: id={}", id);
    }
}

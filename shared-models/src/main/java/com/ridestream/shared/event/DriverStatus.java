package com.ridestream.shared.event;

/**
 * Availability status of a driver.
 *
 * <p>Shared across services because it is part of the {@link LocationUpdate}
 * wire format on the {@code driver-locations} topic. Changing a constant name
 * here is a breaking change for every consumer.
 */
public enum DriverStatus {
    /** online and eligible to be matched. */
    AVAILABLE,
    /** currently assigned to a ride. */
    BUSY,
    /** not accepting rides. */
    OFFLINE
}

package com.ridestream.matching.model;

/**
 * Enum representing the status of a driver.
 */
public enum DriverStatus {
    AVAILABLE,  // Driver is online and ready to accept rides
    BUSY,       // Driver is currently on a ride
    OFFLINE     // Driver is not available
}

package com.ridestream.driver.model;

/**
 * Enum representing the availability status of a driver.
 */
public enum DriverStatus {
    AVAILABLE,  // Driver is online and ready to accept rides
    BUSY,       // Driver is currently on a ride
    OFFLINE     // Driver is not available
}

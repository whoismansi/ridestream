package com.ridestream.driver.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing a vehicle used by a driver.
 */
@Embeddable
@Data
@NoArgsConstructor
public class Vehicle {
    
    private String make;
    private String model;
    private String licensePlate;
    private String color;

    public Vehicle(String make, String model, String licensePlate, String color) {
        this.make = make;
        this.model = model;
        this.licensePlate = licensePlate;
        this.color = color;
    }
}

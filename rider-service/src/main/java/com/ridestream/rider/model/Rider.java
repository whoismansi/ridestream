package com.ridestream.rider.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Rider entity representing a rider in the system
 */
@Entity
@Table(name = "riders", schema = "ridestream")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    @Column(nullable = false, length = 20)
    private String phone;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(5.00);

    @Column(name = "total_rides")
    private Integer totalRides = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

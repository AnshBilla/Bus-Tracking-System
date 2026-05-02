package com.smartRahi.SmartRahi.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drivers",
        indexes = {
                @Index(name = "idx_driver_current_trip", columnList = "current_trip_id"),
                @Index(name = "idx_assigned_bus", columnList = "assigned_bus_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    // Primary Key
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user; // <-- Link to the User account




    // Assigned bus (optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_bus_id", referencedColumnName = "bus_id", unique = true)
    private Bus assignedBus;

    // Optional current trip
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_trip_id", unique = true)
    private Trip currentTrip;

    // Trips assigned to this driver
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
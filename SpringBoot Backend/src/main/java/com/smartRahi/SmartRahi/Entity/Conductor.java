package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conductors", // Naya table
        indexes = {
                @Index(name = "idx_conductor_current_trip", columnList = "current_trip_id"),
                @Index(name = "idx_conductor_assigned_bus", columnList = "assigned_bus_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ⭐️ YEH LINK SABSE IMPORTANT HAI
    // Yeh 'Conductor' (operational) ko 'User' (login) se jodta hai
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    // --- Operational Fields ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_bus_id", referencedColumnName = "bus_id", unique = true)
    private Bus assignedBus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_trip_id", unique = true)
    private Trip currentTrip;

    // Yeh maan kar ki 'Trip' entity mein 'conductor' field bhi hai
    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
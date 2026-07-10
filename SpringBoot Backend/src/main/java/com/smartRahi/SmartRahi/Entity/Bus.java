package com.smartRahi.SmartRahi.Entity;


import com.smartRahi.SmartRahi.enums.BusType;
import com.smartRahi.SmartRahi.enums.OccupancyStatus;
import com.smartRahi.SmartRahi.enums.OperationalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "buses",
        indexes = {
                @Index(name = "idx_bus_current_trip", columnList = "current_trip_id"),
                @Index(name = "idx_operational_status", columnList = "operational_status"),
                @Index(name = "idx_occupancy_status", columnList = "occupancy_status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Bus {

    // Primary Key -> Prisma id String @id @default(uuid())
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Business unique bus ID
    @Column(name = "bus_id", unique = true, nullable = false)
    private String busId;

    @Column(name = "bus_number", unique = true, nullable = false)
    private String busNumber;

    // Enums
    @Enumerated(EnumType.STRING)
    @Column(name = "bus_type", nullable = false)
    private BusType busType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false)
    private OperationalStatus operationalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_status", nullable = false)
    private OccupancyStatus occupancyStatus;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    // Optional current trip (one-to-one)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_trip_id", unique = true)
    private Trip currentTrip;

    // All trips this bus has taken (one-to-many)
    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", unique = true) // Database mein 'trip_id' column banayega
    private Trip trip;

    // Drivers assigned to this bus (many-to-many)
    @ManyToMany
    @JoinTable(
            name = "bus_drivers",
            joinColumns = @JoinColumn(name = "bus_id"),
            inverseJoinColumns = @JoinColumn(name = "driver_id")
    )
    private List<Driver> drivers;

    // Trip metrics recorded for this bus (one-to-many)
    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripMetric> tripMetrics;

    // Optional real-time fields (from old entity)
    @ManyToOne
    @JoinColumn(name = "route_route_id",referencedColumnName = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "next_stop_id")
    private Stop nextStop;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lon")
    private Double currentLon;

    @Column(name = "last_location_update")
    private Instant lastLocationUpdate;

    @Column(name = "speed")
    private Float speed;
    @Column(name = "current_bearing")
    private Float bearing;

    @Column(name = "live_status")
    private String liveStatus;

    @Column(name = "heading")
    private String heading;

    @CreatedDate // ⭐️ ADDED THIS
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // (You should remove any default value like "= Instant.now()")

    @LastModifiedDate // ⭐️ ADDED THIS
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
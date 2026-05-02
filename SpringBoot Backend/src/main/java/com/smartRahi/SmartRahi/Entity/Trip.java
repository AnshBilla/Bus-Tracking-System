package com.smartRahi.SmartRahi.Entity;
import com.smartRahi.SmartRahi.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_gtfs_id", columnList = "gtfs_trip_id", unique = true),
        @Index(name = "idx_trip_service_id", columnList = "service_id"),
        @Index(name = "idx_trip_shape_id", columnList = "shape_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue
    @Column(name = "trip_id", updatable = false, nullable = false)
    private UUID tripId;
    @Column(unique = true, nullable = false)
    private String gtfsTripId; // The GTFS Business ID

    // --- RELATIONS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    // ⭐️ ADD THIS FIELD TO COMPLETE THE RELATIONSHIP
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id") // Name of the foreign key column in the 'trips' table
    private Conductor conductor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_stop_id")
    private Stop startStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_stop_id")
    private Stop finalStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_stop_id")
    private Stop nextStop;

    // --- TRIP DETAILS ---
    @Column(name = "headsign")
    private String headsign;

    @Column(name = "direction")
    private Integer direction;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lon")
    private Double currentLon;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "speed")
    private Float speed;

    @Column(name = "heading")
    private String heading;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatus status = TripStatus.PLANNED;

    // --- EXTRA FIELD ---
    // Store list of stops as JSONB (PostgreSQL)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stops", columnDefinition = "jsonb")
    private String stops; // Or List<YourType>, Map<String, Object>, etc.

    // --- RELATIONSHIP BACKREFERENCES ---
    // Optional mappings if needed later for metrics/complaints
     @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
     private List<TripStop> tripStops;

     @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
     private List<TripMetric> tripMetrics;

     @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
     private List<Complaint> complaints;

     @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
     private List<EmergencyRequest> emergencyRequests;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", referencedColumnName = "id") // Links trips.service_id to services.id
    private Service service;

    private String shapeId; // Foreign Key to Shape
    /**
     * Asli time jab driver ne trip start ki.
     */
    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    /**
     * Asli time jab driver ne trip end ki.
     */
    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

}
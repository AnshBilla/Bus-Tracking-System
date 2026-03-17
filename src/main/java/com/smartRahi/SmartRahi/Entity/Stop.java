package com.smartRahi.SmartRahi.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stops",
        indexes = {
                @Index(name = "idx_city_id", columnList = "city_id"),
                @Index(name = "idx_lat_lon", columnList = "stop_lat, stop_lon")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stop {

    // Primary key: matches Prisma's `id String @id @default(uuid())`
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Unique stop identifier (Prisma: stopId String @unique)
    @Column(name = "stop_id", unique = true, nullable = false)
    private String stopId;

    @Column(name = "stop_name", nullable = false)
    private String stopName;

    @Column(name = "stop_headsign")
    private String stopHeadsign;

    @Column(name = "stop_lat", nullable = false)
    private Double stopLat;

    @Column(name = "stop_lon", nullable = false)
    private Double stopLon;

    // ========== Relation with City ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    // ========== Relation with RouteStop ==========
    @OneToMany(mappedBy = "stop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteStop> routeStops;

    // ========== Relation with TripStop ==========
    @OneToMany(mappedBy = "stop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStop> tripStops;

    // ========== Trips where this stop is start/final/next ==========
    @OneToMany(mappedBy = "startStop", cascade = CascadeType.ALL)
    private List<Trip> tripsAsStartStop;

    @OneToMany(mappedBy = "finalStop", cascade = CascadeType.ALL)
    private List<Trip> tripsAsFinalStop;

    @OneToMany(mappedBy = "nextStop", cascade = CascadeType.ALL)
    private List<Trip> tripsAsNextStop;

    // ========== Routes where this stop is source/destination ==========
    @OneToMany(mappedBy = "sourceStop", cascade = CascadeType.ALL)
    private List<Route> sourceRoutes;

    @OneToMany(mappedBy = "destinationStop", cascade = CascadeType.ALL)
    private List<Route> destinationRoutes;
}
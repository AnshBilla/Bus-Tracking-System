package com.smartRahi.SmartRahi.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    // Primary key corresponding to Prisma's `id String @id @default(uuid())`
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Unique business ID, similar to Prisma's `routeId @unique`
    @Column(name = "route_id", unique = true, nullable = false)
    private String routeId;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Source Stop (start point)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_stop_id", nullable = true)
    private Stop sourceStop;

    // Destination Stop (end point)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_stop_id", nullable = true)
    private Stop destinationStop;

    // ========== Relations ==========

    // One route can have many intermediate RouteStops
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteStop> routeStops;

    // One route can have many trips
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips;

    // One route can have multiple route shapes (e.g., path geometry)
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteShape> routeShapes;
}
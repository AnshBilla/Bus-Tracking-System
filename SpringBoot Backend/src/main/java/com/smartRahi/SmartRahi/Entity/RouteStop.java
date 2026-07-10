package com.smartRahi.SmartRahi.Entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_stops",
        indexes = {
                @Index(name = "idx_route_sequence", columnList = "route_id, stop_sequence")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {

    // Primary Key -> Prisma: id Int @id @default(autoincrement())
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    // ========== Relations ==========
    // Each RouteStop belongs to a specific Route
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // Each RouteStop belongs to a specific Stop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    // ========== Core Prisma Fields ==========
    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "arrival_offset")
    private Integer arrivalOffset; // optional offset (like Prisma's Int?)

    // ========== Custom (extra useful fields from your version) ==========
    @Column(name = "distance_from_start")
    private Double distanceFromStart; // in kilometers (optional)

    @Column(name = "estimated_time_from_start")
    private Integer estimatedTimeFromStart; // in minutes (optional)
}
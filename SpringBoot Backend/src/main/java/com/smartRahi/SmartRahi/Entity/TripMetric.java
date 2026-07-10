package com.smartRahi.SmartRahi.Entity;
import com.smartRahi.SmartRahi.enums.OccupancyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trip_metrics",
        indexes = {
                @Index(name = "idx_tripmetric_trip_id_recorded_at", columnList = "trip_id, recorded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // Prisma's Int @default(autoincrement())

    // ---------- RELATIONS ----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    // ---------- FIELDS ----------
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lon")
    private Double currentLon;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "heading")
    private String heading;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_status")
    private OccupancyStatus occupancyStatus;
}
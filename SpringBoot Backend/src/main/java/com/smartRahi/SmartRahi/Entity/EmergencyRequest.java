package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "emergency_requests",
        indexes = {
                @Index(name = "idx_emergency_trip_id", columnList = "trip_id"),
                @Index(name = "idx_emergency_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequest {

    @Id
    @GeneratedValue
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId; // Prisma: String @default(uuid())

    // ---------- RELATIONS ----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    // ---------- LOCATION ----------
    @Column(name = "location_lat", nullable = false)
    private Double locationLat;

    @Column(name = "location_lon", nullable = false)
    private Double locationLon;

    // ---------- TIMESTAMP ----------
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
package com.smartRahi.SmartRahi.Entity;

import com.smartRahi.SmartRahi.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "complaints",
        indexes = {
                @Index(name = "idx_complaint_trip_id", columnList = "trip_id"),
                @Index(name = "idx_complaint_user_id", columnList = "user_id"),
                @Index(name = "idx_complaint_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue
    @Column(name = "complaint_id", updatable = false, nullable = false)
    private UUID complaintId; // Prisma: String @default(uuid())

    // ----------- RELATIONS -----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    // ----------- FIELDS -----------
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "via_phone", nullable = false)
    private boolean viaPhone = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
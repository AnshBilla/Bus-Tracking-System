
package com.smartRahi.SmartRahi.Entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "trip_stops", indexes = {
        @Index(name = "idx_trip_id", columnList = "tripId"),
        @Index(name = "idx_stop_id", columnList = "stopId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- RELATIONS ----------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    // ---------------- TIME FIELDS ----------------
    @Column(name = "expected_arrival_time")
    private LocalDateTime expectedArrivalTime;

    @Column(name = "expected_departure_time")
    private LocalDateTime expectedDepartureTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "calculated_arrival_time")
    private LocalDateTime calculatedArrivalTime;

    @Column(name = "calculated_departure_time")
    private LocalDateTime calculatedDepartureTime;

    // ---------------- SEQUENCE ----------------
    @Column(name = "stop_sequence", nullable = false)
    private int stopSequence;
}
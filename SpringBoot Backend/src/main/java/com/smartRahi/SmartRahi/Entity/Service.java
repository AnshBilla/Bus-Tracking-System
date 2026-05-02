package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // ⭐️ FIX: Let JPA generate the ID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "gtfs_service_id", nullable = false, unique = true)
    private String gtfsServiceId;

    @Column(name = "monday", nullable = false)
    private Boolean monday = true;

    @Column(name = "tuesday", nullable = false)
    private Boolean tuesday = true;

    @Column(name = "wednesday", nullable = false)
    private Boolean wednesday = true;

    @Column(name = "thursday", nullable = false)
    private Boolean thursday = true;

    @Column(name = "friday", nullable = false)
    private Boolean friday = true;

    @Column(name = "saturday", nullable = false)
    private Boolean saturday = false;

    @Column(name = "sunday", nullable = false)
    private Boolean sunday = false;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
}
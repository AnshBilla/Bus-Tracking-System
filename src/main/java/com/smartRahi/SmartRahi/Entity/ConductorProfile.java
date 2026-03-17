package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "conductor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorProfile {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String employeeId;

    @Column(nullable = false)
    private String aadhar;
}
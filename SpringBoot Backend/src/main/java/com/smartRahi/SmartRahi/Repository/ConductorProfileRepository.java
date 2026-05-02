// RENAMED FILE: ConductorProfileRepository.java
package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.ConductorProfile;
import com.smartRahi.SmartRahi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// This repository manages the ConductorProfile (credentials), NOT the operational Conductor
public interface ConductorProfileRepository extends JpaRepository<ConductorProfile, UUID> {

    // You will need this to find the profile from the user
    Optional<ConductorProfile> findByUser(User user);
}
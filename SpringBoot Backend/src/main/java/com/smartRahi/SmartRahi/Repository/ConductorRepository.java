// NEW FILE: ConductorRepository.java
package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Conductor;
import com.smartRahi.SmartRahi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// This is the correct repository for the OPERATIONAL Conductor entity
public interface ConductorRepository extends JpaRepository<Conductor, UUID> {

    // This checks if a User is already an active conductor
    boolean existsByUser(User user);

    // This finds the operational conductor from their User account
    Optional<Conductor> findByUser(User user);
}
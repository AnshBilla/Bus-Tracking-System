package com.smartRahi.SmartRahi.Repository;


import com.smartRahi.SmartRahi.Entity.Driver;
import com.smartRahi.SmartRahi.Entity.DriverProfile;
import com.smartRahi.SmartRahi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {

    Optional<Object> findByUser(User user);
}
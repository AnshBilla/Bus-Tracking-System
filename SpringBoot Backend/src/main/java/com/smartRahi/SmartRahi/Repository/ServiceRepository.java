package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface  ServiceRepository extends JpaRepository<Service, UUID> {
}

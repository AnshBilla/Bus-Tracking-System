package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Sahi primary key type

@Repository
// JpaRepository<Entity, PrimaryKeyType>
public interface RouteRepository extends JpaRepository<Route, UUID> {

    // GTFS business key (String) se search karne ke liye methods
    Optional<Route> findByRouteId(String routeId);

    boolean existsByRouteId(String routeId);

    void deleteByRouteId(String routeId);
}
package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository  /// responsible for talking to the database and CRUD operations
public interface BusRepository extends JpaRepository<Bus, UUID> {

    // add findByBusNumber if required
    Optional<Bus> findByBusId(String busId);
    void deleteByBusId(String busId);

    // NEW: Check for existence by its String Business ID
    boolean existsByBusId(String busId);
    Optional<Bus> findByTrip(Trip trip);

    Optional<Bus> findByTrip_GtfsTripId(String gtfsTripId);
    @Query("SELECT b FROM Bus b JOIN FETCH b.trip t JOIN FETCH t.route")
    List<Bus> findAllLiveBusesWithTrips();

}
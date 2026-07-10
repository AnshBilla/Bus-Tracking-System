package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Driver;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Is import ko rakha gaya hai
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {


    // Live buses dhoondhne ke liye (BusRealtimeService)
    List<Trip> findByStatusAndLastLocationUpdateAfter(TripStatus status, LocalDateTime threshold);

    // Kisi route par live buses dhoondhne ke liye (BusRealtimeService)
    List<Trip> findByStatusAndRouteAndLastLocationUpdateAfter(TripStatus status, Route route, LocalDateTime threshold);

    // Merge conflict fix: Yeh method rakha gaya hai (GTFS ID se dhoondhne ke liye)
    Optional<Trip> findByGtfsTripId(String gtfsTripId);
    Optional<Trip> findByBusAndStatus(Bus bus, TripStatus status);
    Optional<Trip> findByDriverAndStatus(Driver driver, TripStatus status);

}
package com.smartRahi.SmartRahi.Repository;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.RouteStop;
import com.smartRahi.SmartRahi.Entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    // ⭐️ ADD THIS METHOD: Find all RouteStops associated with a specific Stop
    /**
     * Finds all route segments that include the specified stop.
     * Useful for determining which routes serve a particular stop.
     * @param stop The Stop entity.
     * @return A list of RouteStop entities associated with the given stop.
     */
    List<RouteStop> findByStop(Stop stop);

    // Optional: Add method to find RouteStops for a specific route, ordered by sequence
    List<RouteStop> findByRouteOrderByStopSequenceAsc(Route route);
}
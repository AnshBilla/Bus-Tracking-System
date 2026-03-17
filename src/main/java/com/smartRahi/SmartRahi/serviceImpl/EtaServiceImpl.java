package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.response.EtaResponse;
import com.smartRahi.SmartRahi.Entity.RouteStop;
import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.Repository.RouteStopRepository;
import com.smartRahi.SmartRahi.Repository.StopRepository;
import com.smartRahi.SmartRahi.Repository.TripRepository;
import com.smartRahi.SmartRahi.Services.EtaService;
import com.smartRahi.SmartRahi.enums.TripStatus;
// Import ResourceNotFoundException if you created it
// import com.smartRahi.SmartRahi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime; // Or Instant
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtaServiceImpl implements EtaService {

    private final TripRepository tripRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;

    // Define a default average speed in km/h if real-time speed isn't available
    @Value("${app.eta.default-average-speed-kmh:30.0}")
    private double defaultAverageSpeedKmh;

    // Earth radius in kilometers for distance calculation
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    @Transactional(readOnly = true)
    public EtaResponse calculateEtaForTripStop(UUID tripId, String targetStopBusinessId) {

        // 1. Fetch the active Trip
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty() || tripOpt.get().getStatus() != TripStatus.ACTIVE) {
            log.warn("ETA calculation failed: Trip {} not found or not active.", tripId);
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Trip not active or not found");
        }
        Trip trip = tripOpt.get();

        // 2. Check if trip has necessary data (location, route)
        if (trip.getCurrentLat() == null || trip.getCurrentLon() == null || trip.getRoute() == null) {
            log.warn("ETA calculation failed: Trip {} missing location or route data.", tripId);
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Bus location or route data missing");
        }

        // 3. Find the target Stop entity
        Stop targetStop = stopRepository.findByStopId(targetStopBusinessId)
                .orElse(null);
        if (targetStop == null) {
            log.warn("ETA calculation failed: Target stop {} not found.", targetStopBusinessId);
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Target stop not found");
        }

        // 4. Get the sequence of stops for the trip's route
        List<RouteStop> stopsOnRoute = routeStopRepository.findByRouteOrderByStopSequenceAsc(trip.getRoute());
        if (stopsOnRoute.isEmpty()) {
            log.warn("ETA calculation failed: No stops defined for route {}.", trip.getRoute().getRouteId());
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Route stops not defined");
        }

        // 5. Find the *next* stop the bus is approaching or currently at (based on location)
        //    (This is a simplification - needs refinement)
        RouteStop currentOrNextRouteStop = findCurrentOrNextStop(trip, stopsOnRoute);
        if (currentOrNextRouteStop == null) {
            log.warn("ETA calculation failed: Could not determine current position on route for trip {}.", tripId);
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Bus position on route undetermined");
        }

        // 6. Find the target stop in the sequence
        Optional<RouteStop> targetRouteStopOpt = stopsOnRoute.stream()
                .filter(rs -> rs.getStop().getId().equals(targetStop.getId()))
                .findFirst();
        if (targetRouteStopOpt.isEmpty()) {
            log.warn("ETA calculation failed: Target stop {} not part of route {}.", targetStopBusinessId, trip.getRoute().getRouteId());
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Stop not on this bus's route");
        }
        RouteStop targetRouteStop = targetRouteStopOpt.get();

        // 7. Ensure target stop is *after* the current/next stop in the sequence
        if (targetRouteStop.getStopSequence() < currentOrNextRouteStop.getStopSequence()) {
            log.info("Bus on trip {} has already passed stop {}.", tripId, targetStopBusinessId);
            // Or maybe the bus is at the stop? Refine logic needed.
            return buildUnavailableResponse(tripId.toString(), targetStopBusinessId, "Bus has likely passed this stop");
        }

        // 8. Calculate total distance from current bus location to target stop
        //    (Summing distances between intermediate stops + distance from bus to first intermediate stop)
        double totalDistanceKm = calculateRemainingDistance(trip, currentOrNextRouteStop, targetRouteStop, stopsOnRoute);

        // 9. Estimate time
        double speedKmh = (trip.getSpeed() != null && trip.getSpeed() > 5) ? trip.getSpeed() : defaultAverageSpeedKmh; // Use real speed if available and plausible
        if (speedKmh <= 0) speedKmh = defaultAverageSpeedKmh; // Safety check

        double timeHours = totalDistanceKm / speedKmh;
        long timeSeconds = (long) (timeHours * 3600);
        Duration timeRemaining = Duration.ofSeconds(timeSeconds);

        // Calculate ETA time
        LocalDateTime now = LocalDateTime.now(); // Or Instant.now()
        LocalDateTime estimatedArrivalTime = now.plus(timeRemaining);

        log.info("ETA calculated for Trip {}: Stop={}, Dist={}km, Speed={}kmh, Time={}s, ETA={}",
                tripId, targetStopBusinessId, String.format("%.2f", totalDistanceKm), String.format("%.1f", speedKmh), timeSeconds, estimatedArrivalTime);

        return EtaResponse.builder()
                .tripId(tripId.toString())
                .stopId(targetStopBusinessId)
                .stopName(targetStop.getStopName())
                .estimatedArrivalTime(estimatedArrivalTime)
                .timeRemaining(timeRemaining)
                .distanceRemainingKm(totalDistanceKm)
                .isEstimateAvailable(true)
                .message("Approaching") // Basic message
                .build();
    }

    // Helper to build a response when ETA cannot be calculated
    private EtaResponse buildUnavailableResponse(String tripId, String stopId, String message) {
        return EtaResponse.builder()
                .tripId(tripId)
                .stopId(stopId)
                .isEstimateAvailable(false)
                .message(message)
                .build();
    }

    // --- Helper Methods ---

    /**
     * Finds the RouteStop the bus is currently approaching or has just passed.
     * (This is a simplified implementation - more robust logic needed for accuracy)
     */
    private RouteStop findCurrentOrNextStop(Trip trip, List<RouteStop> stopsOnRoute) {
        if (stopsOnRoute.isEmpty()) return null;

        // Simplistic: Find the closest stop in the sequence to the bus's current location
        // A better approach would project the bus location onto the route shape.
        RouteStop closestStop = null;
        double minDistance = Double.MAX_VALUE;

        for (RouteStop rs : stopsOnRoute) {
            double dist = haversineDistance(
                    trip.getCurrentLat(), trip.getCurrentLon(),
                    rs.getStop().getStopLat(), rs.getStop().getStopLon()
            );
            if (dist < minDistance) {
                minDistance = dist;
                closestStop = rs;
            }
        }
        // Basic assumption: the bus is heading towards the *next* stop after the closest one found.
        // Needs much more complex logic to be reliable (consider direction, shape, progress).
        int closestIndex = stopsOnRoute.indexOf(closestStop);
        if (closestStop != null && closestIndex < stopsOnRoute.size() -1 ){
            // Let's assume for now the "currentOrNext" is the closest one identified
            return closestStop; // Needs refinement!!
        }

        return closestStop; // Return closest if it's the last stop or only one stop
    }

    /**
     * Calculates the total remaining distance along the route stops.
     * (Simplified: uses straight-line distances between stops)
     */
    private double calculateRemainingDistance(Trip trip, RouteStop startRouteStop, RouteStop endRouteStop, List<RouteStop> allStopsOnRoute) {
        double totalDistance = 0.0;

        // 1. Distance from current bus location to the 'startRouteStop' (which is the next or current stop)
        totalDistance += haversineDistance(
                trip.getCurrentLat(), trip.getCurrentLon(),
                startRouteStop.getStop().getStopLat(), startRouteStop.getStop().getStopLon()
        );

        // 2. Sum distances between intermediate stops
        boolean started = false;
        Stop prevStop = startRouteStop.getStop(); // Start summing from here
        for (RouteStop currentRs : allStopsOnRoute) {
            if (currentRs.getId().equals(startRouteStop.getId())) {
                started = true;
                continue; // Don't add distance to the starting stop itself
            }
            if (!started) continue; // Skip stops before the startRouteStop

            Stop currentStop = currentRs.getStop();
            totalDistance += haversineDistance(
                    prevStop.getStopLat(), prevStop.getStopLon(),
                    currentStop.getStopLat(), currentStop.getStopLon()
            );
            prevStop = currentStop; // Move to next segment

            // Stop summing once we reach the target stop
            if (currentRs.getId().equals(endRouteStop.getId())) {
                break;
            }
        }
        return totalDistance;
    }


    /**
     * Calculates the great-circle distance between two points
     * using the Haversine formula.
     * @return Distance in kilometers.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }
}
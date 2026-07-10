package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.LocationUpdateRequest;
import com.smartRahi.SmartRahi.DTO.response.LiveBusResponse; // ⭐️ Import DTO
import com.smartRahi.SmartRahi.Entity.*;
import com.smartRahi.SmartRahi.Repository.*;
import com.smartRahi.SmartRahi.Services.BusRealtimeService;
import com.smartRahi.SmartRahi.enums.Role;
import com.smartRahi.SmartRahi.enums.TripStatus; // ⭐️ Import TripStatus
// Import ResourceNotFoundException if you created it
// import com.smartRahi.SmartRahi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // ⭐️ Import Value
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration; // ⭐️ Import Duration
import java.time.Instant;
import java.time.LocalDateTime; // Use LocalDateTime if your entity uses it
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j // Optional: for logging
public class BusRealtimeServiceImpl implements BusRealtimeService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;       // To find active Driver by User
    private final ConductorRepository conductorRepository; // To find active Conductor by User
    private final BusRepository busRepository;             // To save Bus updates
    private final TripRepository tripRepository;           // To save Trip updates (alternative)
    private final RouteRepository routeRepository;         // ⭐️ Inject RouteRepository
    private final StopRepository stopRepository;           // ⭐️ Inject StopRepository

    // ⭐️ Define a threshold for how old location data can be (e.g., 5 minutes)
    // You can set this in application.properties: app.realtime.location-max-age-minutes=5
    @Value("${app.realtime.location-max-age-minutes:5}")
    private long locationMaxAgeMinutes;


    @Override
    @Transactional
    public void updateBusLocation(String staffUsername, LocationUpdateRequest request) {
        // 1. Find the User account for the logged-in staff
        User user = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + staffUsername));
        // .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + staffUsername));

        // 2. Find the *active operational* Driver or Conductor entity linked to this User
        Bus busToUpdate = null;
        Trip currentTrip = null;

        if (user.getRole() == Role.driver) {
            Driver driver = driverRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Active driver record not found for user: " + staffUsername));
            // .orElseThrow(() -> new ResourceNotFoundException("Active driver record not found for user: " + staffUsername));
            currentTrip = driver.getCurrentTrip(); // Get trip from driver
            if (currentTrip != null) {
                busToUpdate = currentTrip.getBus();
            } else {
                busToUpdate = driver.getAssignedBus();
            }

        } else if (user.getRole() == Role.operator) {
            Conductor conductor = conductorRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Active conductor record not found for user: " + staffUsername));
            // .orElseThrow(() -> new ResourceNotFoundException("Active conductor record not found for user: " + staffUsername));
            currentTrip = conductor.getCurrentTrip(); // Get trip from conductor
            if (currentTrip != null) {
                busToUpdate = currentTrip.getBus();
            } else {
                busToUpdate = conductor.getAssignedBus();
            }
        }

        if (busToUpdate == null) {
            log.warn("No active bus or trip found for staff member: {}", staffUsername);
            throw new RuntimeException("No active bus or trip assignment found for staff member: " + staffUsername);
        }

        // 3. Update the Bus entity fields
        busToUpdate.setCurrentLat(request.getLatitude());
        busToUpdate.setCurrentLon(request.getLongitude());
        busToUpdate.setSpeed(request.getSpeed());
        busToUpdate.setHeading(request.getHeading());
        busToUpdate.setOccupancyStatus(request.getOccupancy());

        LocalDateTime updateTimeLocal = (request.getTimestamp() != null) ? request.getTimestamp() : LocalDateTime.now();
     Instant updateTimeInstant = updateTimeLocal.toInstant(ZoneOffset.UTC);
        busToUpdate.setLastLocationUpdate(updateTimeInstant);
        // 4. Update Trip entity fields (optional)
        if (currentTrip != null) {
            currentTrip.setCurrentLat(request.getLatitude());
            currentTrip.setCurrentLon(request.getLongitude());
            currentTrip.setSpeed(request.getSpeed());
            currentTrip.setHeading(request.getHeading());
            currentTrip.setLastLocationUpdate(updateTimeLocal);            tripRepository.save(currentTrip);
        }

        // 5. Save the updated Bus entity
        busRepository.save(busToUpdate);

        log.info("Updated location for bus {} (Trip: {})", busToUpdate.getBusNumber(), currentTrip != null ? currentTrip.getTripId() : "N/A");
    }

    /** ⭐️ IMPLEMENT THE NEW METHOD
     * Retrieves active buses with recent location updates.
     */
    @Override
    @Transactional(readOnly = true) // Use read-only transaction for queries
    public List<LiveBusResponse> getActiveBuses(String routeBusinessId) {
        // 1. Define the cutoff time for "recent" updates
        LocalDateTime recentThreshold = LocalDateTime.now().minusMinutes(locationMaxAgeMinutes);
        // If using Instant: Instant recentThreshold = Instant.now().minus(Duration.ofMinutes(locationMaxAgeMinutes));

        // 2. Find ACTIVE trips with recent updates
        List<Trip> activeTrips;
        if (routeBusinessId != null && !routeBusinessId.isEmpty()) {
            // Find the Route entity by its business ID
            Route route = routeRepository.findByRouteId(routeBusinessId)
                    .orElse(null);
            if (route == null) {
                log.warn("Route not found for business ID: {}", routeBusinessId);
                return Collections.emptyList(); // Return empty list if route doesn't exist
            }
            // Query trips for the specific route (Requires new method in TripRepository)
            activeTrips = tripRepository.findByStatusAndRouteAndLastLocationUpdateAfter(
                    TripStatus.ACTIVE, route, recentThreshold
            );
        } else {
            // Query all active trips with recent updates (Requires new method in TripRepository)
            activeTrips = tripRepository.findByStatusAndLastLocationUpdateAfter(
                    TripStatus.ACTIVE, recentThreshold
            );
        }

        // 3. Map Trip entities to LiveBusResponse DTOs
        return activeTrips.stream()
                .map(trip -> {
                    Bus bus = trip.getBus();
                    Route route = trip.getRoute();
                    Stop nextStopEntity = trip.getNextStop(); // Assumes Trip has a 'nextStop' field

                    if (bus == null || route == null) {
                        log.warn("Trip {} is active but missing Bus or Route link.", trip.getTripId());
                        return null; // Skip trips with missing essential data
                    }

                    return LiveBusResponse.builder()
                            .busId(bus.getBusId())
                            .busNumber(bus.getBusNumber())
                            .routeId(route.getRouteId())
                            .routeName(route.getRouteName())
                            .tripId(trip.getTripId().toString())
                            .currentLat(trip.getCurrentLat()) // Use location from Trip
                            .currentLon(trip.getCurrentLon())
                            .heading(trip.getHeading())
                            .speed(trip.getSpeed())
                            .occupancyStatus(bus.getOccupancyStatus()) // Occupancy likely on Bus
                            .lastUpdate(trip.getLastLocationUpdate()) // Use timestamp from Trip
                            .nextStopId(nextStopEntity != null ? nextStopEntity.getStopId() : null)
                            .nextStopName(nextStopEntity != null ? nextStopEntity.getStopName() : null)
                            // .nextStopSequence(...) // Need logic to determine this
                            .build();
                })
                .filter(Objects::nonNull) // Remove any nulls from mapping errors
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void updateLocationByBusId(String busBusinessId, LocationUpdateRequest request) {

        // 1. Bus ko uske unique ID (Bus Number ya Vehicle ID) se dhoondein
        Bus busToUpdate = busRepository.findByBusId(busBusinessId) // Maan lete hain BusRepository mein yeh method hai
                .orElseThrow(() -> new RuntimeException("Bus not found with ID: " + busBusinessId));

        // 2. Bus ki location update karein
        LocalDateTime updateTimeLocal = (request.getTimestamp() != null) ? request.getTimestamp() : LocalDateTime.now();

        busToUpdate.setCurrentLat(request.getLatitude());
        busToUpdate.setCurrentLon(request.getLongitude());
        busToUpdate.setSpeed(request.getSpeed());
        busToUpdate.setHeading(request.getHeading());
        busToUpdate.setOccupancyStatus(request.getOccupancy());
        busToUpdate.setLastLocationUpdate(updateTimeLocal.toInstant(ZoneOffset.UTC)); // Ya jo bhi aapka format hai

        // 3. Is 'Bus' ke liye 'ACTIVE' trip dhoondein
        //    (Maan lete hain TripRepository mein yeh method hai)
        Optional<Trip> optionalTrip = tripRepository.findByBusAndStatus(busToUpdate, TripStatus.ACTIVE);

        // 4. Agar active trip milti hai, toh uski location bhi update karein
        if (optionalTrip.isPresent()) {
            Trip currentTrip = optionalTrip.get();
            currentTrip.setCurrentLat(request.getLatitude());
            currentTrip.setCurrentLon(request.getLongitude());
            currentTrip.setSpeed(request.getSpeed());
            currentTrip.setHeading(request.getHeading());
            currentTrip.setLastLocationUpdate(updateTimeLocal);

            tripRepository.save(currentTrip);
        }

        // 5. Bus ko save karein
        busRepository.save(busToUpdate);

        log.info("Updated location for bus {} (via GPS Module)", busBusinessId);
    }

}
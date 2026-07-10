package com.smartRahi.SmartRahi.Controller;

// DTO imports
import com.smartRahi.SmartRahi.DTO.response.JourneyPlanResponse;
import com.smartRahi.SmartRahi.DTO.response.LiveBusResponse;
import com.smartRahi.SmartRahi.DTO.response.SmartStopResponseDTO; // Stops near me ke liye
import com.smartRahi.SmartRahi.DTO.response.StopDetailDTO;    // Phase 2 ke liye
import com.smartRahi.SmartRahi.DTO.response.StopResponse;

// Service imports
import com.smartRahi.SmartRahi.Services.BusRealtimeService;
import com.smartRahi.SmartRahi.Services.JourneyPlannerService;
import com.smartRahi.SmartRahi.Services.StopService;

// Spring aur Lombok imports
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
// import java.util.UUID; // Abhi iski zaroorat nahi hai

@RestController
@RequestMapping("/api/passenger") // Base path for passenger features
@RequiredArgsConstructor
public class PassengerController {

    // Inject the service that will contain the logic
    private final BusRealtimeService busRealtimeService;
    private final StopService stopService;
    private final JourneyPlannerService journeyPlannerService;

    /**
     * Endpoint for passengers to get live locations of active buses.
     */
    @GetMapping("/buses/live")
    @PreAuthorize("hasAnyRole('PASSENGER', 'GUEST')")
    public ResponseEntity<List<LiveBusResponse>> getLiveBuses(
            @RequestParam(required = false) String routeId
    ) {
        List<LiveBusResponse> liveBuses = busRealtimeService.getActiveBuses(routeId);
        return ResponseEntity.ok(liveBuses);
    }

    /**
     * Endpoint to search stops by name.
     */
    @GetMapping("/stops/search")
    @PreAuthorize("hasAnyRole('PASSENGER', 'GUEST')")
    public ResponseEntity<List<StopResponse>> searchStops(
            @RequestParam String query // The search term provided by the user
    ) {
        List<StopResponse> stops = stopService.searchStopsByName(query);
        return ResponseEntity.ok(stops);
    }

    /**
     * Endpoint to find nearby stops based on latitude and longitude.
     */
    @GetMapping("/stops/near")
    @PreAuthorize("hasAnyRole('PASSENGER', 'GUEST')")
    public ResponseEntity<List<SmartStopResponseDTO>> getStopsNearMe(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") double radius,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<SmartStopResponseDTO> stops = stopService.getNearbyStops(lat, lon, radius, pageNumber, pageSize);
        return ResponseEntity.ok(stops);
    }

    /**
     * PHASE 1: Journey Planner API
     * Passenger isse do stops ke beech ki saari (static timed) buses dekhega.
     */
    @GetMapping("/journey")
    @PreAuthorize("hasAnyRole('PASSENGER', 'GUEST')")
    public ResponseEntity<JourneyPlanResponse> planJourney(
            @RequestParam String fromStopName, // <-- Changed to Name
            @RequestParam String toStopName    // <-- Changed to Name
    ) {
        JourneyPlanResponse response = journeyPlannerService.planJourney(fromStopName, toStopName);

        // Response check
        if (response.getOptions() == null || response.getOptions().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PHASE 2: Detailed Route View API
     * Ek specific trip ke, do stops ke beech ke saare stops dikhata hai.
     */
    @GetMapping("/journey/details")
    @PreAuthorize("hasAnyRole('PASSENGER', 'GUEST')")
    public ResponseEntity<List<StopDetailDTO>> getJourneyDetails(
            @RequestParam String gtfsTripId, // Trip ID (Phase 1 se mila)
            @RequestParam String fromStopId, // Start stop
            @RequestParam String toStopId    // End stop
    ) {
        List<StopDetailDTO> stopDetails = journeyPlannerService.getTripStopDetails(gtfsTripId, fromStopId, toStopId);

        if (stopDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stopDetails);
    }
}
package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.LocationUpdateRequest;
import com.smartRahi.SmartRahi.Services.BusRealtimeService; // We will create this service interface
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // To get the logged-in user
import java.util.UUID;

@RestController
@RequestMapping("/api/bus/realtime") // Base path for real-time bus actions
@RequiredArgsConstructor
public class BusRealtimeController {

    private final BusRealtimeService busRealtimeService;

    // Endpoint for driver/bus to send location updates
    @PutMapping("/location")
    @PreAuthorize("hasAnyRole('DRIVER', 'operator')") // Only drivers or conductors can update
    public ResponseEntity<?> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            Principal principal // Spring Security provides the logged-in user
    ) {
        // We assume the service knows which bus/trip the logged-in user (principal) is currently assigned to
        busRealtimeService.updateBusLocation(principal.getName(), request);
        return ResponseEntity.ok().build(); // Just acknowledge receipt
    }

    // You could add other endpoints here later, e.g., startTrip, endTrip, updateOccupancy
}
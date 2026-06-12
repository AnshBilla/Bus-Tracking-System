package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.DriverRequest;
import com.smartRahi.SmartRahi.DTO.request.StartTripRequest;
import com.smartRahi.SmartRahi.DTO.response.DriverResponse;
import com.smartRahi.SmartRahi.Services.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    @PostMapping("/trip/start")
    public ResponseEntity<String> startTrip(
            @Valid @RequestBody StartTripRequest request,
            Authentication authentication // Logged-in driver ki details ke liye
    ) {
        // 'authentication.getName()' se humein logged-in user ka username milega
        String driverUsername = authentication.getName();

        driverService.startTrip(driverUsername, request);

        return ResponseEntity.ok("Trip " + request.getGtfsTripId() + " started successfully.");
    }

    /**
     * API: Driver ki duty/trip khatm karne ke liye
     */
    @PostMapping("/trip/end")
    public ResponseEntity<String> endTrip(Authentication authentication) {

        String driverUsername = authentication.getName();

        driverService.endTrip(driverUsername);

        return ResponseEntity.ok("Trip ended successfully.");
    }

    @PostMapping
    public DriverResponse createDriver(@RequestBody DriverRequest request) {
        return driverService.createDriver(request);
    }

    @GetMapping("/{driverId}")
    public DriverResponse getDriverById(@PathVariable UUID driverId) {
        return driverService.getDriverById(driverId);
    }

    @GetMapping
    public List<DriverResponse> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @PutMapping("/{driverId}")
    public DriverResponse updateDriver(@PathVariable UUID driverId, @RequestBody DriverRequest request) {
        return driverService.updateDriver(driverId, request);
    }

    @DeleteMapping("/{driverId}")
    public void deleteDriver(@PathVariable UUID driverId) {
        driverService.deleteDriver(driverId);
    }
}

package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.TripRequest;
import com.smartRahi.SmartRahi.DTO.response.TripResponse;
import com.smartRahi.SmartRahi.Services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public TripResponse createTrip(@RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTripById(@PathVariable String tripId) {
        try {
            // Agar Admin Panel ne UUID bheja hai toh ye chalega
            UUID uuid = UUID.fromString(tripId);
            return tripService.getTripById(uuid);
        } catch (IllegalArgumentException e) {
            // Agar Frontend ne "10205" bheja (jo UUID nahi hai), toh ye catch hoke chalega!
            return tripService.getTripByGtfsId(tripId);
        }
    }

    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    @PutMapping("/{tripId}")
    public TripResponse updateTrip(@PathVariable UUID tripId, @RequestBody TripRequest request) {
        return tripService.updateTrip(tripId, request);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId) {
        tripService.deleteTrip(tripId);
    }
}

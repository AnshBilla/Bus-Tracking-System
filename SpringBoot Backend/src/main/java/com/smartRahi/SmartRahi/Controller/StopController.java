package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.StopRequest;
import com.smartRahi.SmartRahi.DTO.response.SmartStopResponseDTO;
import com.smartRahi.SmartRahi.DTO.response.StopResponse;
import com.smartRahi.SmartRahi.Repository.Projections.NearbyStopProjection;
import com.smartRahi.SmartRahi.Services.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final StopService stopService;


    @PostMapping
    public StopResponse createStop(@RequestBody StopRequest request) {
        return stopService.createStop(request);
    }

    @GetMapping("/{stopId}")
    public StopResponse getStopById(@PathVariable String stopId) {
        return stopService.getStopById(stopId);
    }

    @GetMapping
    public List<StopResponse> getAllStops() {
        return stopService.getAllStops();
    }

    @PutMapping("/{stopId}")
    public StopResponse updateStop(@PathVariable String stopId, @RequestBody StopRequest request) {
        return stopService.updateStop(stopId, request);
    }
    @GetMapping("/near")
    public ResponseEntity<List<SmartStopResponseDTO>> getNearbyStops(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") double radius, // 1000 meters
            @RequestParam(defaultValue = "0") int page,         // Page 0 (pehla page)
            @RequestParam(defaultValue = "10") int size         // Ek page par 10 result
    ) {
        // Page number (0) ko offset (0) mein badalna
        // Page number (1) ko offset (10) mein badalna
        int pageNumber = (page < 0) ? 0 : page;
        int pageSize = (size <= 0) ? 10 : size;

        List<SmartStopResponseDTO> stops = stopService.getNearbyStops(lat, lon, radius, pageNumber, pageSize);

        return ResponseEntity.ok(stops);
    }


    @DeleteMapping("/{stopId}")
    public void deleteStop(@PathVariable String stopId) {
        stopService.deleteStop(stopId);
    }
}

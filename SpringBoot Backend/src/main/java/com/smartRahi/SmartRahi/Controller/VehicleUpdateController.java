package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.LocationUpdateRequest;
import com.smartRahi.SmartRahi.Services.BusRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle") // ⭐️ Naya, alag path
@RequiredArgsConstructor
public class VehicleUpdateController {

    private final BusRealtimeService busRealtimeService;

    /**
     * Yeh naya API hai.
     * Bus mein laga GPS module is API ko har 15 second mein call karega.
     *
     * @param busBusinessId Yeh bus ka unique ID hai (jaise "DL1CA1234" ya "bus-001")
     * @param request       Ismein Lat, Lon, Speed, etc. hai
     */
    @PutMapping("/update/{busBusinessId}")
    public ResponseEntity<Void> updateVehicleLocation(
            @PathVariable String busBusinessId,
            @RequestBody LocationUpdateRequest request) {

        // Yeh wahi service method call karega jo humne pehle design kiya tha
        busRealtimeService.updateLocationByBusId(busBusinessId, request);
        return ResponseEntity.ok().build();
    }
}
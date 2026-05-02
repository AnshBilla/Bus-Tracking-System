package com.smartRahi.SmartRahi.Controller; // Or your correct package

import com.smartRahi.SmartRahi.DTO.request.CityRequest;
import com.smartRahi.SmartRahi.DTO.response.CityResponse; // Import the DTO
import com.smartRahi.SmartRahi.Services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // Import UUID

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    // Returns CityResponse, takes CityRequest
    @PostMapping
    public ResponseEntity<CityResponse> createCity(@RequestBody CityRequest cityRequest) {
        CityResponse newCity = cityService.createCity(cityRequest);
        return new ResponseEntity<>(newCity, HttpStatus.CREATED);
    }

    // Returns List<CityResponse>
    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        List<CityResponse> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    // Returns CityResponse, takes @PathVariable UUID
    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getCityById(@PathVariable UUID id) {
        CityResponse city = cityService.getCityById(id);
        return ResponseEntity.ok(city);
    }

    // Returns CityResponse, takes @PathVariable UUID and CityRequest
    @PutMapping("/{id}")
    public ResponseEntity<CityResponse> updateCity(@PathVariable UUID id, @RequestBody CityRequest cityRequest) {
        CityResponse updatedCity = cityService.updateCity(id, cityRequest);
        return ResponseEntity.ok(updatedCity);
    }

    // Takes @PathVariable UUID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable UUID id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}
package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.CityRequest;
import com.smartRahi.SmartRahi.DTO.response.CityResponse; // IMPORT
import com.smartRahi.SmartRahi.Entity.City;
import com.smartRahi.SmartRahi.Repository.CityRepository;
import com.smartRahi.SmartRahi.exception.ResourceNotFoundException;
import com.smartRahi.SmartRahi.mapper.CityMapper; // IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors; // IMPORT

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    /**
     * Finds a city by its UUID or throws 404.
     * This is a private helper method.
     */
    private City findCityById(UUID id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
    }

    /**
     * Creates a new city and returns its DTO.
     */
    public CityResponse createCity(CityRequest request) { // RETURN CityResponse
        City newCity = City.builder()
                .cityName(request.cityName())
                .build();

        City savedCity = cityRepository.save(newCity);
        return CityMapper.toResponse(savedCity); // Map to response
    }

    /**
     * Returns a List of all cities as DTOs.
     */
    public List<CityResponse> getAllCities() { // RETURN List<CityResponse>
        return cityRepository.findAll()
                .stream()
                .map(CityMapper::toResponse) // Map each city
                .collect(Collectors.toList());
    }

    /**
     * Finds a single city by its ID and returns its DTO.
     */
    public CityResponse getCityById(UUID id) { // RETURN CityResponse
        City city = findCityById(id); // Find the entity
        return CityMapper.toResponse(city); // Map to response
    }

    /**
     * Updates an existing city and returns its DTO.
     */
    public CityResponse updateCity(UUID id, CityRequest request) { // RETURN CityResponse
        City existingCity = findCityById(id); // Find entity
        existingCity.setCityName(request.cityName());

        City updatedCity = cityRepository.save(existingCity);
        return CityMapper.toResponse(updatedCity); // Map to response
    }

    /**
     * Deletes a city by its ID.
     */
    public void deleteCity(UUID id) {
        // Find the existing city (this will throw 404 if it doesn't exist)
        City existingCity = findCityById(id);

        // Delete it
        cityRepository.delete(existingCity);
    }
}
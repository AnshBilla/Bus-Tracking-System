package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.response.CityResponse;
import com.smartRahi.SmartRahi.DTO.response.StopResponse;
import com.smartRahi.SmartRahi.Entity.City;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections; // Import this for safety

public class CityMapper {

    public static CityResponse toResponse(City city) {
        if (city == null) {
            return null;
        }

        // Create a list of StopResponse DTOs
        List<StopResponse> stopResponses;
        if (city.getStops() != null) {
            stopResponses = city.getStops().stream()
                    .map(StopMapper::toResponse) // Use the StopMapper we already fixed
                    .collect(Collectors.toList());
        } else {
            stopResponses = Collections.emptyList(); // Handle null list
        }

        return CityResponse.builder()
                .cityId(String.valueOf(city.getCityId()))
                .cityName(city.getCityName())
                .stops(stopResponses) // Set the list of DTOs
                .build();
    }
}
package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.TripRequest;
import com.smartRahi.SmartRahi.DTO.response.TripResponse;

import java.util.List;
import java.util.UUID;

public interface TripService {
    TripResponse createTrip(TripRequest request);
    TripResponse getTripById(UUID tripId);
    TripResponse getTripByGtfsId(String gtfsTripId);
    List<TripResponse> getAllTrips();
    TripResponse updateTrip(UUID tripId, TripRequest request);
    void deleteTrip(UUID tripId);
}
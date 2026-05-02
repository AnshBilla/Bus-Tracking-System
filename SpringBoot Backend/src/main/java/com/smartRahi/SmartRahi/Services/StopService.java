package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.StopRequest;
import com.smartRahi.SmartRahi.DTO.response.SmartStopResponseDTO;
import com.smartRahi.SmartRahi.DTO.response.StopResponse;
// In imports ki zaroorat nahi hai (Map, UUID, NearbyStopProjection)
import java.util.List;

public interface StopService {

    StopResponse createStop(StopRequest request);

    StopResponse getStopById(String stopId);

    List<StopResponse> getAllStops();

    // Conflict merge kiya (String stopId wala version rakha)
    StopResponse updateStop(String stopId, StopRequest request);

    // Conflict merge kiya (String stopId wala version rakha)
    void deleteStop(String stopId);

    // Conflict merge kiya (SmartStopResponseDTO wala version rakha)
    // Note: Parameters ko pageNumber aur pageSize mein badal diya hai
    // taaki yeh StopServiceImpl se match karein.
    List<SmartStopResponseDTO> getNearbyStops(double lat, double lon, double radius, int pageNumber, int pageSize);

    /**
     * Searches for stops by name.
     *
     * @param query The partial name to search for.
     * @return A list of StopResponse DTOs matching the query.
     */
    List<StopResponse> searchStopsByName(String query);
}
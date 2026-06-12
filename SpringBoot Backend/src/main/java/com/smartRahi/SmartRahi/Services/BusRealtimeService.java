package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.LocationUpdateRequest;
import com.smartRahi.SmartRahi.DTO.response.LiveBusResponse;

import java.util.List;

public interface BusRealtimeService {

    /**
     * Updates the location and status of the bus/trip associated with the currently logged-in staff member.
     * @param staffUsername The username of the logged-in driver or operator.
     * @param request The location data received.
     */
    void updateBusLocation(String staffUsername, LocationUpdateRequest request);
    List<LiveBusResponse> getActiveBuses(String routeBusinessId);
    void updateLocationByBusId(String busBusinessId, LocationUpdateRequest request);

}
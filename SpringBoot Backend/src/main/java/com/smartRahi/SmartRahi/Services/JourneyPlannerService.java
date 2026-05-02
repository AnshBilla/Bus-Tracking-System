package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.response.JourneyPlanResponse;
import com.smartRahi.SmartRahi.DTO.response.StopDetailDTO; // Phase 2 ke liye import
import java.util.List; // Phase 2 ke liye import

public interface JourneyPlannerService {

    /**
     * PHASE 1:
     * Finds potential journeys between two stops.
     *
     * @param fromStopBusinessId The business ID (String) of the starting stop.
     * @param toStopBusinessId The business ID (String) of the destination stop.
     * @return A JourneyPlanResponse containing direct route options, if any.
     */
    JourneyPlanResponse planJourney(String fromStopBusinessId, String toStopBusinessId);

    /**
     * PHASE 2:
     * Gets the list of intermediate stops for a specific trip between two stops.
     *
     * @param gtfsTripId The GTFS trip ID (e.g., "51874")
     * @param fromStopId The business ID of the starting stop for this leg
     * @param toStopId The business ID of the destination stop for this leg
     * @return A list of StopDetailDTO objects, ordered by stop_sequence.
     */
    List<StopDetailDTO> getTripStopDetails(String gtfsTripId, String fromStopId, String toStopId);
}
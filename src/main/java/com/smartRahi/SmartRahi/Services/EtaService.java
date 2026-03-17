package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.response.EtaResponse;
import java.util.List;
import java.util.UUID;

public interface EtaService {

    /**
     * Calculates the Estimated Time of Arrival (ETA) for a specific active trip
     * at a specific stop on its route.
     *
     * @param tripId        The UUID of the currently active trip.
     * @param targetStopId  The business ID (String) of the stop for which ETA is needed.
     * @return An EtaResponse containing the estimated arrival time and other details.
     */
    EtaResponse calculateEtaForTripStop(UUID tripId, String targetStopId);

    /**
     * (Future Method Example)
     * Gets upcoming departures (active trips with ETAs) for a specific stop.
     *
     * @param stopBusinessId The business ID (String) of the stop.
     * @return A list of EtaResponse objects for upcoming buses.
     */
    // List<EtaResponse> getUpcomingDeparturesForStop(String stopBusinessId);
}
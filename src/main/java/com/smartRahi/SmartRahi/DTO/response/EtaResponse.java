package com.smartRahi.SmartRahi.DTO.response;

import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime; // Or Instant

@Data
@Builder
public class EtaResponse {
    private String tripId;
    private String stopId;
    private String stopName;
    private LocalDateTime estimatedArrivalTime; // Calculated ETA time
    private Duration timeRemaining;          // Duration until arrival (e.g., "PT5M30S")
    private Double distanceRemainingKm;      // Calculated distance
    private boolean isEstimateAvailable;     // Flag if calculation succeeded
    private String message;                  // e.g., "Approaching", "Delayed", "Calculation unavailable"
}
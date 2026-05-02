// New File: com/smartRahi/SmartRahi/DTO/response/SmartStopResponseDTO.java

package com.smartRahi.SmartRahi.DTO.response;

import lombok.Data;
import java.util.List;

/**
 * Yeh hamara naya "smart" response hai.
 * Yeh stop ki info (Phase 1) aur live arrivals (Phase 2) ko jodta hai.
 */
@Data
public class SmartStopResponseDTO {

    // Stop ki info (aapke NearbyStopProjection se)
    private String stopId;
    private String stopName;
    private Double stopLat;
    private Double stopLon;
    private Double distance; // User se kitni door hai

    // Smart Hissa: Iss stop par aane waali live buses
    private List<LiveBusArrivalDTO> liveArrivals;
}
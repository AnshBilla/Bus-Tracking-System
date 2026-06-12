package com.smartRahi.SmartRahi.DTO.response;

import com.smartRahi.SmartRahi.enums.OccupancyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime; // Or Instant, matching your entity

@Data
@Builder
public class LiveBusResponse {
    private String busId;           // The Bus's business ID
    private String busNumber;       // e.g., "MH-12 AB-1234"
    private String routeId;         // Route ID the bus is currently on
    private String routeName;       // Route name
    private String tripId;          // Current trip UUID
    private Double currentLat;
    private Double currentLon;
    private String heading;         // e.g., "N", "SW", degrees

    // Merge conflict fix: 'Double' chuna gaya hai
    private Float speed;           // e.g., km/h

    private OccupancyStatus occupancyStatus; // Enum value
    private LocalDateTime lastUpdate; // Or Instant
    private String nextStopId;      // Business ID of the next stop
    private String nextStopName;    // Name of the next stop
    private Integer nextStopSequence; // Sequence number of the next stop on the route
}
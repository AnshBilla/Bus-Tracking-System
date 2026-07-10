package com.smartRahi.SmartRahi.DTO.request;

import com.smartRahi.SmartRahi.enums.OccupancyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    // Merge conflict fix: 'Double' chuna gaya hai
    private Float speed;       // Optional speed in km/h or m/s

    private String heading;     // Optional direction (e.g., "N", "SW", or degrees)
    private OccupancyStatus occupancy; // Optional current occupancy

    // Optional: Timestamp from the device, otherwise server uses current time
    private LocalDateTime timestamp;
}
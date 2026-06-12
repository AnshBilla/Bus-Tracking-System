package com.smartRahi.SmartRahi.DTO.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;

@Data
public class StartTripRequest {

    @NotEmpty(message = "GTFS Trip ID is required")
    private String gtfsTripId; // Jaise "51874"

    @NotEmpty(message = "Bus ID is required")
    private String busId; // Jaise "bus-001" ya "DL1CA1234"
}
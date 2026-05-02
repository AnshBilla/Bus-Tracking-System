package com.smartRahi.SmartRahi.DTO.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JourneyOption {
    private String type; // e.g., "DIRECT"
    private List<JourneyLeg> legs;
    private String estimatedDuration; // Placeholder for now, e.g., "N/A"
}
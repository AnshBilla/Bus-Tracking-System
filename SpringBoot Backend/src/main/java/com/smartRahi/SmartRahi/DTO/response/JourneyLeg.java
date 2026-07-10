package com.smartRahi.SmartRahi.DTO.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JourneyLeg {

    // Merge conflict fix: Is field ko rakha gaya hai
    private String gtfsTripId;

    private String routeId;
    private String routeName;
    private String fromStopId;
    private String fromStopName;
    private String toStopId;
    private String toStopName;
    private List<LiveBusResponse> liveBuses; // Buses currently on this route
    private String estimatedDeparture;
    private String estimatedArrival;
}
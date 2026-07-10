package com.smartRahi.SmartRahi.DTO.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns bus details.
public class BusResponse {
    private UUID id;
    private String busId;
    private String busNumber;
    private String busType;
    private Integer capacity;
    private String operationalStatus;
    private String occupancyStatus;
    private Double currentLat;
    private Double currentLon;
    private String routeId;
    private String nextStopId;
}
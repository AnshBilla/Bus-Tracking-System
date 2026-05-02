package com.smartRahi.SmartRahi.DTO.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns bus stop details.
public class StopResponse {
    private UUID id;
    private String stopId;
    private String stopName;
    private String stopHeadsign;
    private Double stopLat;
    private Double stopLon;
}
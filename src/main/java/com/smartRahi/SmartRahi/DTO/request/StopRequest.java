package com.smartRahi.SmartRahi.DTO.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
///Used to create or update a bus stop.
public class StopRequest {
    private String stopId;
    private String stopName;
    private String stopHeadsign;
    private Double stopLat;
    private Double stopLon;
    private UUID cityId;
}
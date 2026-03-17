package com.smartRahi.SmartRahi.DTO.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Used to create or update a trip
public class TripRequest {
    private String routeId;
    private String headsign;
    private Integer direction;
    private String busId;
    private String busNumber;
    private String driverId;
    private String stops; // JSON string for stops
}
package com.smartRahi.SmartRahi.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns trip details.
public class TripResponse {
    private String tripId;
    private String routeId;
    private String headsign;
    private Integer direction;
    private String busId;
    private String busNumber;
    private String driverId;
    private String stops;
}
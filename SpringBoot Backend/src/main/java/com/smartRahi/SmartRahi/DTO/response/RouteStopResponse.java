package com.smartRahi.SmartRahi.DTO.response;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns details of a stop inside a route.
public class RouteStopResponse {
    private Integer id;
    private String routeId;
    private String stopId;
    private Integer stopSequence;
    private String stopName;
    private double latitude;
    private double longitude;
    private Double distance;
}

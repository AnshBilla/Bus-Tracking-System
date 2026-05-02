package com.smartRahi.SmartRahi.DTO.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Used to add or update a stop inside a route.
public class RouteStopRequest {
    private String routeId;
    private String stopId;
    private Integer stopSequence;
}
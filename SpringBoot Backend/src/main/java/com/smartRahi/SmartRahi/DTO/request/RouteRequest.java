package com.smartRahi.SmartRahi.DTO.request;

import lombok.*;
/// Used to create or update a route.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequest {
    private String routeId;
    private String routeName;
    private String sourceStopId;
    private String destinationStopId;
}
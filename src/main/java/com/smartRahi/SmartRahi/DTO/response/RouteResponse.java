package com.smartRahi.SmartRahi.DTO.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns route details.
public class RouteResponse {

    private String routeId;
    private String routeName;
    private String sourceStopId;
    private String destinationStopId;
}
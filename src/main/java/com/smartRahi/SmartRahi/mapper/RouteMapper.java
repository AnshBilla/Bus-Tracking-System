package com.smartRahi.SmartRahi.mapper;


import com.smartRahi.SmartRahi.DTO.request.RouteRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteResponse;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Stop;

import java.util.UUID;

public class RouteMapper {


    public static Route toEntity(RouteRequest req, Stop source, Stop destination) {
        return Route.builder()
                .routeId(req.getRouteId())
                .routeName(req.getRouteName())
                .sourceStop(source)
                .destinationStop(destination)
                .isActive(true)
                .build();
    }

    public static RouteResponse toResponse(Route route) {
        return RouteResponse.builder().routeId(route.getRouteId())
                .routeName(route.getRouteName())
                .sourceStopId(route.getSourceStop() != null ? route.getSourceStop().getStopId().toString() : null)
                .destinationStopId(route.getDestinationStop() != null ? route.getDestinationStop().getStopId().toString() : null)
                .build();
    }
}





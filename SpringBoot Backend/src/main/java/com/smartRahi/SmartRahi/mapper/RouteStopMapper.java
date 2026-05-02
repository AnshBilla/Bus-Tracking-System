package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.request.RouteStopRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteStopResponse;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.RouteStop;
import com.smartRahi.SmartRahi.Entity.Stop;

public class RouteStopMapper {

    public static RouteStop toEntity(RouteStopRequest req, Route route, Stop stop) {
        return RouteStop.builder()
                .route(route)
                .stop(stop)
                .stopSequence(req.getStopSequence())
                .build();
    }

    public static RouteStopResponse toResponse(RouteStop rs) {
        return RouteStopResponse.builder()
                .id(rs.getId())
                .routeId(rs.getRoute().getRouteId())
                .stopId(rs.getStop().getStopId())
                .stopSequence(rs.getStopSequence())
                .stopId(rs.getStop().getStopId())
                .distance(rs.getDistanceFromStart())
                .stopName(rs.getStop().getStopName())
                .latitude(rs.getStop().getStopLat())   // Make sure the field name is correct
                .longitude(rs.getStop().getStopLon())
                .build();
    }
}
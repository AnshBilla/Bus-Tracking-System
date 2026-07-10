package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.RouteStopRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteStopResponse;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface RouteStopService {
    RouteStopResponse createRouteStop(RouteStopRequest request);
    RouteStopResponse getRouteStopById(UUID routeStopId);
    List<RouteStopResponse> getAllRouteStops();
    RouteStopResponse updateRouteStop(UUID routeStopId, RouteStopRequest request);
    void deleteRouteStop(UUID routeStopId);
    List<RouteStopResponse> getRouteStopsSortedByDistance(Double userLat, Double userLon);
}
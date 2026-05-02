package com.smartRahi.SmartRahi.Services;


import com.smartRahi.SmartRahi.DTO.request.RouteRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteResponse;

import java.util.List;
import java.util.UUID;

public interface RouteService {
    RouteResponse createRoute(RouteRequest request);
    RouteResponse getRouteById(String routeId);
    List<RouteResponse> getAllRoutes();
    RouteResponse updateRoute(String routeId, RouteRequest request);
    void deleteRoute(String routeId);
}
package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.RouteRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteResponse;
import com.smartRahi.SmartRahi.Services.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public RouteResponse createRoute(@RequestBody RouteRequest request) {
        return routeService.createRoute(request);
    }

    @GetMapping("/{routeId}")
    public RouteResponse getRouteById(@PathVariable String routeId) {
        return routeService.getRouteById(routeId);
    }

    @GetMapping
    public List<RouteResponse> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @PutMapping("/{routeId}")
    public RouteResponse updateRoute(@PathVariable String routeId, @RequestBody RouteRequest request) {
        return routeService.updateRoute(routeId, request);
    }

    @DeleteMapping("/{routeId}")
    public void deleteRoute(@PathVariable String routeId) {
        routeService.deleteRoute(routeId);
    }
}


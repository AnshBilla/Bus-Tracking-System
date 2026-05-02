package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.RouteStopRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteStopResponse;
import com.smartRahi.SmartRahi.Services.RouteStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/route-stops")
@RequiredArgsConstructor
public class RouteStopController {

    private final RouteStopService routeStopService;

    @PostMapping
    public RouteStopResponse createRouteStop(@RequestBody RouteStopRequest request) {
        return routeStopService.createRouteStop(request);
    }

    @GetMapping("/{routeStopId}")
    public RouteStopResponse getRouteStopById(@PathVariable UUID routeStopId) {
        return routeStopService.getRouteStopById(routeStopId);
    }
    @GetMapping
    public List<RouteStopResponse> getAllRouteStops(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
    ) {
        if (userLat != null && userLon != null) {
            return routeStopService.getRouteStopsSortedByDistance(userLat, userLon);
        }
        return routeStopService.getAllRouteStops();
    }



    @PutMapping("/{routeStopId}")
    public RouteStopResponse updateRouteStop(@PathVariable UUID routeStopId, @RequestBody RouteStopRequest request) {
        return routeStopService.updateRouteStop(routeStopId, request);
    }

    @DeleteMapping("/{routeStopId}")
    public void deleteRouteStop(@PathVariable UUID routeStopId) {
        routeStopService.deleteRouteStop(routeStopId);
    }
}

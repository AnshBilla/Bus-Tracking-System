package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.RouteStopRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteStopResponse;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.RouteStop;
import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Repository.RouteRepository;
import com.smartRahi.SmartRahi.Repository.RouteStopRepository;
import com.smartRahi.SmartRahi.Repository.StopRepository;
import com.smartRahi.SmartRahi.Services.RouteStopService;
import com.smartRahi.SmartRahi.mapper.RouteStopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.awt.geom.Point2D.distance;

@Service
@RequiredArgsConstructor
public class RouteStopServiceImpl implements RouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    @Override
    public RouteStopResponse createRouteStop(RouteStopRequest request) {
        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Stop stop = stopRepository.findByStopId(request.getStopId())
                .orElseThrow(() -> new RuntimeException("Stop not found"));

        RouteStop routeStop = RouteStopMapper.toEntity(request, route, stop);
        RouteStop saved = routeStopRepository.save(routeStop);
        return RouteStopMapper.toResponse(saved);
    }

    @Override
    public RouteStopResponse getRouteStopById(UUID routeId) {
        RouteStop rs = routeStopRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("RouteStop not found"));
        return RouteStopMapper.toResponse(rs);
    }

    @Override
    public List<RouteStopResponse> getAllRouteStops() {
        return routeStopRepository.findAll().stream()
                .map(RouteStopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RouteStopResponse updateRouteStop(UUID routeStopId, RouteStopRequest request) {
        RouteStop rs = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new RuntimeException("RouteStop not found"));

        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Stop stop = stopRepository.findById(UUID.fromString(request.getStopId()))
                .orElseThrow(() -> new RuntimeException("Stop not found"));

        rs.setRoute(route);
        rs.setStop(stop);
        rs.setStopSequence(request.getStopSequence());

        RouteStop updated = routeStopRepository.save(rs);
        return RouteStopMapper.toResponse(updated);
    }

    @Override
    public void deleteRouteStop(UUID routeStopId) {
        if (!routeStopRepository.existsById(routeStopId)) {
            throw new RuntimeException("RouteStop not found");
        }
        routeStopRepository.deleteById(routeStopId);
    }
    @Override
    public List<RouteStopResponse> getRouteStopsSortedByDistance(Double userLat, Double userLon) {
        return routeStopRepository.findAll().stream()
                .sorted(Comparator.comparingDouble(routeStop ->
                        distance(
                                userLat,
                                userLon,
                                routeStop.getStop().getStopLat(),
                                routeStop.getStop().getStopLon()
                        )
                ))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    // ✅ Helper to map entity → response DTO
    private RouteStopResponse mapToResponse(RouteStop routeStop) {
        return RouteStopResponse.builder()
                .id(routeStop.getId())
                .routeId(routeStop.getRoute().getRouteId())
                .stopId(routeStop.getStop().getStopId())
                .stopName(routeStop.getStop().getStopName())
                .stopSequence(routeStop.getStopSequence())
                .build();
    }
    // ✅ Haversine formula for distance calculation
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
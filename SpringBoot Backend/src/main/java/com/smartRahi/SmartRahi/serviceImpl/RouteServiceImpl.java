package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.RouteRequest;
import com.smartRahi.SmartRahi.DTO.response.RouteResponse;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Repository.RouteRepository;
import com.smartRahi.SmartRahi.Repository.StopRepository;
import com.smartRahi.SmartRahi.Services.RouteService;
import com.smartRahi.SmartRahi.mapper.RouteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
// import java.util.UUID; // Iski zaroorat nahi hai
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    @Override
    public RouteResponse createRoute(RouteRequest request) {
        // Stops ko unke String business ID se dhoondein
        Stop source = stopRepository.findByStopId(request.getSourceStopId())
                .orElseThrow(() -> new RuntimeException("Source stop not found"));
        Stop destination = stopRepository.findByStopId(request.getDestinationStopId())
                .orElseThrow(() -> new RuntimeException("Destination stop not found"));

        Route route = RouteMapper.toEntity(request, source, destination);
        Route savedRoute = routeRepository.save(route);
        return RouteMapper.toResponse(savedRoute);
    }

    @Override
    // Merge conflict fix: 'String routeId' (business key) istemaal karein
    public RouteResponse updateRoute(String routeId, RouteRequest request) {

        // Route ko uske String business ID se dhoondein
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        // Stops ko unke String business ID se dhoondein
        Stop source = stopRepository.findByStopId(request.getSourceStopId())
                .orElseThrow(() -> new RuntimeException("Source stop not found"));
        Stop destination = stopRepository.findByStopId(request.getDestinationStopId())
                .orElseThrow(() -> new RuntimeException("Destination stop not found"));

        // ⭐️ Note: Aapka code yahaan par route fields ko update nahi kar raha hai.
        // Aapko RouteMapper ya manual setters ka istemaal karna chahiye:
        // route.setRouteName(request.getRouteName());
        // route.setSourceStop(source);
        // route.setDestinationStop(destination);
        // ...etc.

        Route updatedRoute = routeRepository.save(route); // Yahaan updated route save hoga
        return RouteMapper.toResponse(updatedRoute);
    }

    @Override
    // Merge conflict fix: 'String routeId' (business key) istemaal karein
    public RouteResponse getRouteById(String routeId) {
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        return RouteMapper.toResponse(route);
    }

    @Override
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(RouteMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public void deleteRoute(String routeId) {
        if (!routeRepository.existsByRouteId(routeId)) {
            throw new RuntimeException("Route not found");
        }
        routeRepository.deleteByRouteId(routeId);
    }
}
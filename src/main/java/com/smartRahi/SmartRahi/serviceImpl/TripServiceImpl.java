package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.TripRequest;
import com.smartRahi.SmartRahi.DTO.response.TripResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Driver;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.DriverRepository;
import com.smartRahi.SmartRahi.Repository.RouteRepository;
import com.smartRahi.SmartRahi.Repository.TripRepository;
import com.smartRahi.SmartRahi.Services.TripService;
import com.smartRahi.SmartRahi.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    @Override
    public TripResponse createTrip(TripRequest request) {
        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Bus bus = busRepository.findById(UUID.fromString(request.getBusId()))
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        Driver driver = driverRepository.findById(UUID.fromString(request.getDriverId()))
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Trip trip = TripMapper.toEntity(request, route, bus, driver);
        Trip saved = tripRepository.save(trip);
        return TripMapper.toResponse(saved);
    }

    @Override
    public TripResponse getTripById(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return TripMapper.toResponse(trip);
    }

    @Override
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(TripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse updateTrip(UUID tripId, TripRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Bus bus = busRepository.findById(UUID.fromString(request.getBusId()))
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        Driver driver = driverRepository.findById(UUID.fromString(request.getDriverId()))
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDriver(driver);

        trip.setHeadsign(request.getHeadsign());
        trip.setDirection(request.getDirection());
        trip.setStops(request.getStops());

        Trip updated = tripRepository.save(trip);
        return TripMapper.toResponse(updated);
    }

    @Override
    public void deleteTrip(UUID tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found");
        }
        tripRepository.deleteById(tripId);
    }
}
package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.BusRequest;
import com.smartRahi.SmartRahi.DTO.response.BusResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.RouteRepository;
import com.smartRahi.SmartRahi.Repository.StopRepository;
import com.smartRahi.SmartRahi.Services.BusService;
import com.smartRahi.SmartRahi.mapper.BusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
///  in service all the business logics are written
@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    @Override
    public BusResponse createBus(BusRequest request) {///  logic for creating a bus
        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Stop stop = stopRepository.findByStopId(request.getNextStopId())
                .orElseThrow(() -> new RuntimeException("Stop not found"));

        Bus bus = BusMapper.toEntity(request, route, stop);
        return BusMapper.toResponse(busRepository.save(bus));
    }

    @Override///  logic for get BUS by ID
    public BusResponse getBusById(String busId) {
        return busRepository.findByBusId(busId)
                .map(BusMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }

    @Override///  logic for getting all buses
    public List<BusResponse> getAllBuses() {
        return busRepository.findAll().stream()
                .map(BusMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BusResponse updateBus(String busId, BusRequest request) {
        Bus bus = busRepository.findByBusId(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        Route route = routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Stop stop = stopRepository.findByStopId(request.getNextStopId())
                .orElseThrow(() -> new RuntimeException("Stop not found"));

        bus.setBusNumber(request.getBusNumber());
        bus.setBusType(request.getBusType());
        bus.setCapacity(request.getCapacity());
        bus.setOperationalStatus(request.getOperationalStatus());
        bus.setOccupancyStatus(request.getOccupancyStatus());
        bus.setRoute(route);
        bus.setNextStop(stop);

        return BusMapper.toResponse(busRepository.save(bus));
    }

    @Override
    public void deleteBus(String busId) {
        if (!busRepository.existsByBusId(busId)) {
            throw new RuntimeException("Bus not found");
        }
        busRepository.deleteByBusId(busId);
    }
}
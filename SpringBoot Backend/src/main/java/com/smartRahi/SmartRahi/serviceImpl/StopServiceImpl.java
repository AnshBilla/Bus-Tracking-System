package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.StopRequest;
import com.smartRahi.SmartRahi.DTO.response.LiveBusArrivalDTO;
import com.smartRahi.SmartRahi.DTO.response.SmartStopResponseDTO;
import com.smartRahi.SmartRahi.DTO.response.StopResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.City;
import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.CityRepository;
import com.smartRahi.SmartRahi.Repository.Projections.NearbyStopProjection;
import com.smartRahi.SmartRahi.Repository.StopRepository;
import com.smartRahi.SmartRahi.Services.StopService;
import com.smartRahi.SmartRahi.Util.HaversineUtil;
import com.smartRahi.SmartRahi.mapper.StopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Saaf kiye gaye imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StopServiceImpl implements StopService {

    private final StopRepository stopRepository;
    private final CityRepository cityRepository;
    private final BusRepository busRepository;

    @Override
    public StopResponse createStop(StopRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found with id: " + request.getCityId()));
        Stop stop = StopMapper.toEntity(request, city);
        Stop saved = stopRepository.save(stop);
        return StopMapper.toResponse(saved);
    }

    @Override
    public StopResponse getStopById(String stopId) {
        Stop stop = stopRepository.findByStopId(stopId)
                .orElseThrow(() -> new RuntimeException("Stop not found"));
        return StopMapper.toResponse(stop);
    }

    @Override
    public List<StopResponse> getAllStops() {
        return stopRepository.findAll().stream()
                .map(StopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StopResponse updateStop(String stopId, StopRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found with id: " + request.getCityId()));
        Stop stop = stopRepository.findByStopId(stopId)
                .orElseThrow(() -> new RuntimeException("Stop not found"));

        stop.setStopName(request.getStopName());
        stop.setStopHeadsign(request.getStopHeadsign());
        stop.setStopLat(request.getStopLat());
        stop.setStopLon(request.getStopLon());
        stop.setCity(city);

        Stop updated = stopRepository.save(stop);
        return StopMapper.toResponse(updated);
    }

    @Override
    public void deleteStop(String stopId) {
        if (!stopRepository.existsByStopId(stopId)) {
            throw new RuntimeException("Stop not found");
        }
        stopRepository.deleteByStopId(stopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartStopResponseDTO> getNearbyStops(double lat, double lon, double radius, int pageNumber, int pageSize) {

        int offset = pageNumber * pageSize;
        int limit = pageSize;

        // --- PHASE 1: SCHEDULE DATA FETCH KAREIN ---
        List<NearbyStopProjection> nearbyStops = stopRepository.findNearbyStops(lat, lon, radius, limit, offset);

        if (nearbyStops.isEmpty()) {
            return Collections.emptyList();
        }

        // --- PHASE 2: LIVE DATA FETCH KAREIN ---
        List<Bus> allLiveBuses = busRepository.findAllLiveBusesWithTrips();

        // --- PHASE 3: DONO KO COMBINE KAREIN (THE "SMART" LOGIC) ---
        List<SmartStopResponseDTO> smartStopList = new ArrayList<>();

        for (NearbyStopProjection stop : nearbyStops) {
            List<LiveBusArrivalDTO> liveArrivals = new ArrayList<>();

            for (Bus bus : allLiveBuses) {
                double distanceToStopKm = HaversineUtil.calculateDistanceKm(
                        bus.getCurrentLat(), bus.getCurrentLon(),
                        stop.getStopLat(), stop.getStopLon()
                );

                if (distanceToStopKm < 5 && bus.getSpeed() != null && bus.getSpeed() > 1) {
                    double distanceToStopMeters = distanceToStopKm * 1000;
                    int etaSeconds = (int) (distanceToStopMeters / bus.getSpeed());
                    int etaMinutes = etaSeconds / 60;

                    if (etaMinutes >= 0 && etaMinutes <= 30) {
                        liveArrivals.add(
                                LiveBusArrivalDTO.builder()
                                        .routeName(bus.getTrip().getRoute().getRouteName())
                                        .headsign(bus.getTrip().getHeadsign())
                                        .busLat(bus.getCurrentLat())
                                        .busLon(bus.getCurrentLon())
                                        .etaMinutes(etaMinutes)
                                        .build()
                        );
                    }
                }
            }

            liveArrivals.sort(Comparator.comparing(LiveBusArrivalDTO::getEtaMinutes));

            SmartStopResponseDTO smartStop = new SmartStopResponseDTO();
            smartStop.setStopId(stop.getStopId());
            smartStop.setStopName(stop.getStopName());
            smartStop.setStopLat(stop.getStopLat());
            smartStop.setStopLon(stop.getStopLon());
            smartStop.setDistance(stop.getDistance());
            smartStop.setLiveArrivals(liveArrivals);

            smartStopList.add(smartStop);
        }

        return smartStopList;
    }

    /**
     * Yeh 'searchStopsByName' ka sahi version hai.
     * Duplicate method hata diya gaya hai.
     */
    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> searchStopsByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList(); // Return empty if query is blank
        }
        // Behtar search method ko call karein (OrderByLength)
        List<Stop> foundStops = stopRepository.findByNameContainingIgnoreCaseOrderByLength(query.trim());

        // Map the results to DTOs
        return foundStops.stream()
                .map(StopMapper::toResponse)
                .collect(Collectors.toList());
    }
}
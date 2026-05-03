package com.smartRahi.SmartRahi.Services;

import com.google.transit.realtime.GtfsRealtime;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.TripRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GtfsRealtimeService {

    private static final Logger log = LoggerFactory.getLogger(GtfsRealtimeService.class);
    private static final String URL = "YOUR_GTFS_URL";

    private final RestTemplate restTemplate;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final EntityManager entityManager;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 15000) // safe scheduler
    @Transactional
    public void pollVehiclePositions() {

        log.info("Polling GTFS Realtime...");

        try {
            InputStream stream = restTemplate.execute(
                    URL,
                    HttpMethod.GET,
                    null,
                    response -> response.getBody()
            );

            if (stream == null) return;

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(stream);

            int count = 0;

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {

                if (!entity.hasVehicle()) continue;

                GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();

                if (!vehicle.hasTrip() || !vehicle.getTrip().hasTripId()) continue;

                String tripId = vehicle.getTrip().getTripId();

                Optional<Bus> busOpt = busRepository.findByTrip_GtfsTripId(tripId);

                Bus bus;

                if (busOpt.isPresent()) {
                    bus = busOpt.get();
                } else {
                    Optional<Trip> tripOpt = tripRepository.findByGtfsTripId(tripId);
                    if (tripOpt.isEmpty()) continue;

                    bus = Bus.builder().trip(tripOpt.get()).build();
                }

                if (vehicle.hasPosition()) {
                    bus.setCurrentLat((double) vehicle.getPosition().getLatitude());
                    bus.setCurrentLon((double) vehicle.getPosition().getLongitude());
                    bus.setSpeed(vehicle.getPosition().getSpeed());
                    bus.setBearing(vehicle.getPosition().getBearing());
                }

                bus.setLiveStatus(vehicle.getCurrentStatus().name());

                entityManager.persist(bus);

                count++;

                // 🔥 BATCH FLUSH (MOST IMPORTANT)
                if (count % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear(); // MEMORY FREE
                }
            }

            // final flush
            entityManager.flush();
            entityManager.clear();

            log.info("Updated {} buses successfully", count);

        } catch (Exception e) {
            log.error("Error in GTFS polling: {}", e.getMessage());
        }
    }
}
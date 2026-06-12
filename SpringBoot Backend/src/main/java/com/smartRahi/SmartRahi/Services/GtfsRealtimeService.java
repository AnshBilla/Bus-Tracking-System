// New File: com.smartRahi.SmartRahi.Service.GtfsRealtimeService.java
// (Aap package ka naam ...serviceImpl bhi rakh sakte hain agar aapko pasand ho)

package com.smartRahi.SmartRahi.Services;

import com.google.transit.realtime.GtfsRealtime;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GtfsRealtimeService {

    private static final Logger log = LoggerFactory.getLogger(GtfsRealtimeService.class);

    // ⭐️ IMPORTANT: Apni transit agency ka live data URL yahaan daalein
    private static final String GTFS_REALTIME_URL = "YOUR_AGENCY_REALTIME_VEHICLE_POSITIONS_URL";
    // Example: "https://api.example.com/gtfs-rt/vehicle-positions"
    // Yeh URL aapko apni local transit agency ki website se milega.

    private final RestTemplate restTemplate;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;

    /**
     * Yeh method har 15 second mein apne aap chalega.
     * fixedDelay = 15000 (milliseconds)
     */
    //@Scheduled(fixedDelay = 15000)
    @Transactional
    public void pollVehiclePositions() {
        log.info("Polling for GTFS Realtime Vehicle Positions...");

        try {
            // 1. Live feed se raw (binary) data fetch karein
            byte[] feedData = restTemplate.getForObject(GTFS_REALTIME_URL, byte[].class);

            if (feedData == null) {
                log.warn("GTFS Realtime feed was empty or null.");
                return;
            }

            // 2. Data ko parse karein
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(feedData);

            List<Bus> busesToUpdate = new ArrayList<>();

            // 3. Har entity ko loop karein (har live bus)
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {

                // ⭐️ YEH RAHA FIX ⭐️
                // Method ka naam 'hasVehicle()' hai
                if (entity.hasVehicle()) {

                    // ⭐️ AUR YEH BHI FIX ⭐️
                    // Method ka naam 'getVehicle()' hai
                    GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();

                    // 4. Sabse zaroori: Live data ko apne database se "join" karein

                    // Safety Check: Kya trip ID maujood hai?
                    if (!vehicle.hasTrip() || !vehicle.getTrip().hasTripId() || vehicle.getTrip().getTripId().isEmpty()) {
                        log.warn("Found vehicle position without a valid tripId. Skipping.");
                        continue;
                    }

                    String gtfsTripId = vehicle.getTrip().getTripId();

                    // Kya humare paas iss trip ke liye pehle se bus hai?
                    Optional<Bus> busOpt = busRepository.findByTrip_GtfsTripId(gtfsTripId);

                    Bus busToUpdate;
                    if (busOpt.isPresent()) {
                        // Haan, bus pehle se hai. Ise update karo.
                        busToUpdate = busOpt.get();
                    } else {
                        // Nahi, yeh bus abhi-abhi live hui hai.
                        // Pehle 'Trip' ko dhoondo...
                        Optional<Trip> tripOpt = tripRepository.findByGtfsTripId(gtfsTripId);
                        if (tripOpt.isEmpty()) {
                            // Agar trip hi nahi hai, toh yeh bus hamare schedule ka hissa nahi hai.
                            log.warn("Found live vehicle for an unknown tripId: {}", gtfsTripId);
                            continue;
                        }
                        // Nayi 'Bus' entity banao aur usse 'Trip' se link karo
                        busToUpdate = Bus.builder().trip(tripOpt.get()).build();
                    }

                    // 5. 'Bus' entity ko live data se update karo
                    // Safety Check: Kya position data maujood hai?
                    if (vehicle.hasPosition()) {
                        busToUpdate.setCurrentLat(Double.valueOf(vehicle.getPosition().getLatitude()));
                        busToUpdate.setCurrentLon(Double.valueOf(vehicle.getPosition().getLongitude()));
                        busToUpdate.setBearing(vehicle.getPosition().getBearing());
                        busToUpdate.setSpeed(vehicle.getPosition().getSpeed());
                    }
                    busToUpdate.setLiveStatus(vehicle.getCurrentStatus().name());

                    busesToUpdate.add(busToUpdate);
                }
            }

            // 6. Saari updated buses ko ek saath database mein save karo (Batch Update)
            busRepository.saveAll(busesToUpdate);
            log.info("Successfully updated/created {} live bus positions.", busesToUpdate.size());

        } catch (Exception e) {
            log.error("Failed to poll or parse GTFS Realtime feed: {}", e.getMessage(), e);
        }
    }
}
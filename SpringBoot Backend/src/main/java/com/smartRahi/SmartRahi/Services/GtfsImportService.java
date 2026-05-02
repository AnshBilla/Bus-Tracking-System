package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.Entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.smartRahi.SmartRahi.Entity.City;

// ...and so on

import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Service; // Explicit import
import com.smartRahi.SmartRahi.Entity.ShapePoint;
import com.smartRahi.SmartRahi.Entity.Stop;

import java.time.LocalTime;
import java.time.ZoneId;
import com.smartRahi.SmartRahi.enums.TripStatus;
import com.smartRahi.SmartRahi.Entity.Trip;
import com.smartRahi.SmartRahi.Repository.*;
import com.smartRahi.SmartRahi.Services.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GtfsImportService {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportService.class);
    private static final int BATCH_SIZE = 25000;

    // Inject all repositories
    private final CityRepository cityRepository;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final ServiceRepository serviceRepository;
    private final ShapeRepository shapeRepository;
    private final ShapePointRepository shapePointRepository;
    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final GeocodingService geocodingService;

    // Use a fixed thread pool for parallel tasks
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // Stats object to track results
    private final Map<String, Integer> stats = new ConcurrentHashMap<>();

    // ID Maps to link GTFS string IDs to our internal UUIDs
    private Map<String, UUID> stopIdMap = new ConcurrentHashMap<>();
    private Map<String, UUID> routeIdMap = new ConcurrentHashMap<>();
    private Map<String, UUID> tripIdMap = new ConcurrentHashMap<>();
    private Map<String, UUID> serviceIdMap = new ConcurrentHashMap<>();
    /**
     * Main ingestion function.
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> ingestGtfs(String gtfsPath, boolean useGeocoding, String defaultCityName) throws Exception {

        // 1. GTFS ko memory mein load karein
        GtfsDaoImpl store = new GtfsDaoImpl();
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(new File(gtfsPath));
        reader.setEntityStore(store);
        reader.run();
        log.info("GTFS data loaded into memory.");

        // 2. Puraana data saaf karein
        cleanupDatabase();
        log.info("Database cleaned.");

        // --- SEQUENTIAL IMPORT (EK-KE-BAAD-EK) ---
        // Yahi hai naya badlaav (fix)

        // 3. City banao aur save karo
        City city = findAndIngestCity(store, useGeocoding, defaultCityName);
        log.info("Ingested city: {}", city.getCityName());

        // 4. Stops (City par depend karta hai)
        ingestStops(store, city);
        log.info("Ingested Stops.");

        // 5. Routes
        ingestRoutes(store);
        log.info("Ingested Routes.");

        // 6. Services (Calendar)
        ingestServices(store);
        log.info("Ingested Services.");

        // 7. Shapes
        ingestShapesAndPoints(store);
        log.info("Ingested Shapes.");

        // 8. Trips (Routes aur Services par depend karta hai)
        ingestTrips(store);
        log.info("Ingested Trips.");

        // 9. TripStops (Trips aur Stops par depend karta hai)
        ingestTripStops(store);
        log.info("Ingested TripStops.");

        // --- IMPORT POORA HUA ---

        log.info("GTFS Ingestion Successful!");
        log.info("--- Ingestion Stats ---");
        stats.forEach((key, value) -> log.info("{}: {}", key, value));
        return stats;
    }

    private void cleanupDatabase() {
        log.info("Cleaning old data...");
        // Delete in reverse order of dependency
        tripStopRepository.deleteAllInBatch();
        shapePointRepository.deleteAllInBatch();
        tripRepository.deleteAllInBatch();
        shapeRepository.deleteAllInBatch();
        serviceRepository.deleteAllInBatch();
        routeRepository.deleteAllInBatch();
        stopRepository.deleteAllInBatch();
        cityRepository.deleteAllInBatch();
        log.info("Database cleaned.");
    }
    private City findAndIngestCity(GtfsDaoImpl store, boolean useGeocoding, String defaultCityName) {
        String cityName = defaultCityName; // Default naam

        if (useGeocoding) {
            try {
                // ⭐️ FIX: Hum explicitly 'org.onebusaway.gtfs.model.Stop' ka istemaal kar rahe hain
                // Taaki 'Entity.Stop' se confusion na ho.
                org.onebusaway.gtfs.model.Stop firstStop = store.getAllStops().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No stops found in GTFS data."));

                // ⭐️ FIX: Ab .getLat() aur .getLon() methods 100% milenge
                // Aur 'reverseGeocode' method (jo hum agle step mein banayenge)
                cityName = geocodingService.getCityFromCoordinates(firstStop.getLat(), firstStop.getLon());

                if (cityName == null || cityName.isEmpty()) {
                    cityName = defaultCityName;
                    log.warn("Geocoding failed, using default city name: {}", defaultCityName);
                }
            } catch (Exception e) {
                log.error("Geocoding service call failed: {}", e.getMessage());
                cityName = defaultCityName;
            }
        }

        City city = City.builder()
                .cityName(cityName)
                .build();

        return cityRepository.save(city);
    }
    // In: GtfsImportService.java

    private void ingestStops(GtfsDaoImpl store, City city) {
        log.info("Ingesting stops...");
        List<Stop> stopsToSave = new ArrayList<>();
        for (org.onebusaway.gtfs.model.Stop gtfsStop : store.getAllStops()) {

            Stop stop = Stop.builder()
                    // .id(UUID.randomUUID()) // <-- ⭐️ YEH LINE HATA DEIN (THE FIX)
                    .stopId(gtfsStop.getId().getId())
                    .stopName(gtfsStop.getName())
                    .stopLat(gtfsStop.getLat())
                    .stopLon(gtfsStop.getLon())
                    .city(city)
                    .build();

            stopsToSave.add(stop);
        }

        // ⭐️ NAYA KADAM: saveAll ab humein naye IDs ke saath list wapas dega
        List<Stop> savedStops = stopRepository.saveAll(stopsToSave);

        // Ab, naye IDs ke saath map bharein
        for (Stop savedStop : savedStops) {
            stopIdMap.put(savedStop.getStopId(), savedStop.getId());
        }

        stats.put("stops", savedStops.size());
    }

    // In: GtfsImportService.java

    private void ingestRoutes(GtfsDaoImpl store) {
        log.info("Ingesting routes...");
        List<Route> routesToSave = new ArrayList<>();
        for (org.onebusaway.gtfs.model.Route gtfsRoute : store.getAllRoutes()) {

            Route route = Route.builder()
                    // .id(UUID.randomUUID()) // <-- ⭐️ YEH LINE HATA DEIN (THE FIX)
                    .routeId(gtfsRoute.getId().getId())
                    .routeName(gtfsRoute.getShortName() != null ? gtfsRoute.getShortName() : gtfsRoute.getLongName())
                    .isActive(true)
                    .build();

            routesToSave.add(route);
        }

        List<Route> savedRoutes = routeRepository.saveAll(routesToSave);

        for (Route savedRoute : savedRoutes) {
            routeIdMap.put(savedRoute.getRouteId(), savedRoute.getId());
        }

        stats.put("routes", savedRoutes.size());
    }
    private void ingestServices(GtfsDaoImpl store) {
        log.info("Ingesting services from calendar.txt...");
        List<Service> servicesToSave = new ArrayList<>();
        for (ServiceCalendar gtfsCalendar : store.getAllCalendars()) {

            Date utilStartDate = gtfsCalendar.getStartDate().getAsDate();
            Date utilEndDate = gtfsCalendar.getEndDate().getAsDate();

            LocalDateTime localStartDate = utilStartDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            LocalDateTime localEndDate = utilEndDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            Service service = Service.builder()
                    // DO NOT SET THE ID MANUALLY
                    .gtfsServiceId(gtfsCalendar.getServiceId().getId()) // Set the new field
                    .monday(gtfsCalendar.getMonday() == 1)
                    .tuesday(gtfsCalendar.getTuesday() == 1)
                    .wednesday(gtfsCalendar.getWednesday() == 1)
                    .thursday(gtfsCalendar.getThursday() == 1)
                    .friday(gtfsCalendar.getFriday() == 1)
                    .saturday(gtfsCalendar.getSaturday() == 1)
                    .sunday(gtfsCalendar.getSunday() == 1)
                    .startDate(localStartDate)
                    .endDate(localEndDate)
                    .build();

            servicesToSave.add(service);
        }

        // Save and populate the map
        List<Service> savedServices = serviceRepository.saveAll(servicesToSave);

        for (Service savedService : savedServices) {
            serviceIdMap.put(savedService.getGtfsServiceId(), savedService.getId());
        }

        stats.put("services", savedServices.size());
    }

    private void ingestShapesAndPoints(GtfsDaoImpl store) {
        log.info("Ingesting shapes and shape points...");
        Set<String> processedShapeIds = new HashSet<>();
        List<Shape> shapesToSave = new ArrayList<>();
        List<ShapePoint> shapePointBatch = new ArrayList<>();
        int totalPoints = 0;

        for (org.onebusaway.gtfs.model.ShapePoint gtfsPoint : store.getAllShapePoints()) {
            String shapeId = gtfsPoint.getShapeId().getId();

            // Create the parent Shape record if new
            if (!processedShapeIds.contains(shapeId)) {
                shapesToSave.add(new Shape(shapeId));
                processedShapeIds.add(shapeId);
            }

            // Create the ShapePoint
            ShapePoint point = new ShapePoint();
            point.setShapeId(shapeId);
            point.setShapePtLat(gtfsPoint.getLat());
            point.setShapePtLon(gtfsPoint.getLon());
            point.setShapePtSequence(gtfsPoint.getSequence());
            if (gtfsPoint.isDistTraveledSet()) {
                point.setShapeDistTraveled(gtfsPoint.getDistTraveled());
            }
            shapePointBatch.add(point);
            totalPoints++;

            // Batching Logic
            if (shapePointBatch.size() >= BATCH_SIZE) {
                log.info("Batch inserting {} shape points...", shapePointBatch.size());
                shapePointRepository.saveAll(shapePointBatch);
                shapePointBatch.clear();
            }
        }

        // Save remaining points
        if (!shapePointBatch.isEmpty()) {
            log.info("Batch inserting final {} shape points...", shapePointBatch.size());
            shapePointRepository.saveAll(shapePointBatch);
        }

        // Save all parent shape records
        shapeRepository.saveAll(shapesToSave);
        stats.put("shapes", processedShapeIds.size());
        stats.put("shapePoints", totalPoints);
    }

    // In: GtfsImportService.java

    // In: GtfsImportService.java

    private void ingestTrips(GtfsDaoImpl store) {
        log.info("Ingesting trips...");
        List<Trip> tripsToSave = new ArrayList<>();

        for (org.onebusaway.gtfs.model.Trip gtfsTrip : store.getAllTrips()) {

            // 1. Get the STRING IDs from the GTFS file
            String gtfsRouteId = gtfsTrip.getRoute().getId().getId();
            String gtfsServiceId = gtfsTrip.getServiceId().getId();

            // 2. Find our internal UUIDs from the maps
            UUID routeUuid = routeIdMap.get(gtfsRouteId);
            UUID serviceUuid = serviceIdMap.get(gtfsServiceId); // <-- Using the new map

            String shapeId = (gtfsTrip.getShapeId() != null) ? gtfsTrip.getShapeId().getId() : null;

            // 3. Check if *both* UUIDs were found
            if (routeUuid == null || serviceUuid == null) {
                log.warn("Skipping trip {} due to missing route ({}) or service ({})",
                        gtfsTrip.getId().getId(), gtfsRouteId, gtfsServiceId);
                continue;
            }

            Trip.TripBuilder tripBuilder = Trip.builder()
                    .gtfsTripId(gtfsTrip.getId().getId())
                    .headsign(gtfsTrip.getTripHeadsign())
                    .status(TripStatus.PLANNED)
                    .shapeId(shapeId)

                    // 4. Use .getReferenceById() to link the full objects
                    .route(routeRepository.getReferenceById(routeUuid))
                    .service(serviceRepository.getReferenceById(serviceUuid)); // <-- Passing the Service object

            if (gtfsTrip.getDirectionId() != null) {
                tripBuilder.direction(Integer.parseInt(gtfsTrip.getDirectionId()));
            }

            Trip trip = tripBuilder.build();
            tripsToSave.add(trip);
        }

        List<Trip> savedTrips = tripRepository.saveAll(tripsToSave);

        // This map is still needed for ingestTripStops()
        for (Trip savedTrip : savedTrips) {
            tripIdMap.put(savedTrip.getGtfsTripId(), savedTrip.getTripId());
        }

        stats.put("trips", savedTrips.size());
    }
    // In: GtfsImportService.java

    // In: GtfsImportService.java

    private void ingestTripStops(GtfsDaoImpl store) {
        log.info("Ingesting trip stops (this may take a while)...");
        List<TripStop> tripStopBatch = new ArrayList<>();
        int totalTripStops = 0;

        for (StopTime gtfsStopTime : store.getAllStopTimes()) {

            UUID tripUuid = tripIdMap.get(gtfsStopTime.getTrip().getId().getId());
            UUID stopUuid = stopIdMap.get(gtfsStopTime.getStop().getId().getId());

            if (tripUuid == null || stopUuid == null) {
                continue;
            }

            TripStop tripStop = TripStop.builder()
                    // .id(UUID.randomUUID()) // <-- ⭐️ YEH LINE HATA DEIN (THE FIX)
                    .stopSequence(gtfsStopTime.getStopSequence())
                    .trip(tripRepository.getReferenceById(tripUuid))
                    .stop(stopRepository.getReferenceById(stopUuid))
                    .expectedArrivalTime(convertGtfsSecondsToLocalDateTime(gtfsStopTime.getArrivalTime()))
                    .expectedDepartureTime(convertGtfsSecondsToLocalDateTime(gtfsStopTime.getDepartureTime()))
                    .build();

            tripStopBatch.add(tripStop);
            totalTripStops++;

            // Batching Logic
            if (tripStopBatch.size() >= BATCH_SIZE) {
                log.info("Batch inserting {} trip stops...", tripStopBatch.size());
                tripStopRepository.saveAll(tripStopBatch); // Yahaan saveAll naye IDs generate karega
                tripStopBatch.clear();
            }
        }

        // Save remaining batch
        if (!tripStopBatch.isEmpty()) {
            log.info("Batch inserting final {} trip stops...", tripStopBatch.isEmpty());
            tripStopRepository.saveAll(tripStopBatch);
        }
        stats.put("tripStops", totalTripStops);
    }
    /**
     * Helper to convert 'onebusaway' int time (seconds since midnight)
     * back to the GTFS HH:MM:SS string format.
     */
    private LocalDateTime convertGtfsSecondsToLocalDateTime(int secondsSinceMidnight) {

        // 1. Use a fixed, "dummy" date.
        // We use 1970-01-01 as a standard "epoch" date.
        LocalDate dummyDate = LocalDate.of(1970, 1, 1);

        // 2. Calculate seconds within a 24-hour day (86400 seconds).
        // This "wraps" times like 25:30:00 (91800s) back to 01:30:00 (5400s).
        // (91800 % 86400 = 5400)
        int secondsInDay = secondsSinceMidnight % 86400;

        // 3. Create the LocalTime from the wrapped seconds.
        LocalTime time = LocalTime.ofSecondOfDay(secondsInDay);

        // 4. Combine the dummy date and the calculated time.
        return LocalDateTime.of(dummyDate, time);
    }
}
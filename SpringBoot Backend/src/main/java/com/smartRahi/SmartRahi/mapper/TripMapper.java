package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.request.TripRequest;
import com.smartRahi.SmartRahi.DTO.response.TripResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Driver;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Trip;

import java.util.UUID;

public class TripMapper {

    public static Trip toEntity(TripRequest req, Route route, Bus bus, Driver driver) {
        return Trip.builder()
                .tripId(UUID.randomUUID())
                .route(route)
                .headsign(req.getHeadsign())
                .direction(req.getDirection())
                .bus(bus)

                .driver(driver)
                .stops(req.getStops()) // JSONB field
                .build();
    }

    public static TripResponse toResponse(Trip trip) {
        return TripResponse.builder()
                .tripId(trip.getTripId().toString())
                .routeId(trip.getRoute() != null ? trip.getRoute().getRouteId().toString() : null)
                .headsign(trip.getHeadsign())
                .direction(trip.getDirection())
                .busId(trip.getBus() != null ? trip.getBus().getBusId().toString() : null)

                .driverId(trip.getDriver() != null ? trip.getDriver().getId().toString() : null)
                .stops(trip.getStops())
                .build();
    }
}
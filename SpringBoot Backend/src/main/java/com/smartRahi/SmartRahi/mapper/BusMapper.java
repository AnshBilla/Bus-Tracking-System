package com.smartRahi.SmartRahi.mapper;


import com.smartRahi.SmartRahi.DTO.request.BusRequest;
import com.smartRahi.SmartRahi.DTO.response.BusResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Route;
import com.smartRahi.SmartRahi.Entity.Stop;

import java.util.UUID;
/// A Mapper converts:
///
/// Request DTO → Entity (for saving in database).
///
/// Entity → Response DTO (for sending back to client).
public class BusMapper {

    // Request → Entity
    public static Bus toEntity(BusRequest req, Route route, Stop stop) {
        return Bus.builder()
                .busId(req.getBusId()) // Always generate new UUID for primary key
                .busNumber(req.getBusNumber()) // User provided bus number
                .busType(req.getBusType())
                .capacity(req.getCapacity())
                .operationalStatus(req.getOperationalStatus())
                .occupancyStatus(req.getOccupancyStatus())
                .currentLat(req.getCurrentLat())
                .currentLon(req.getCurrentLon())
                .route(route)
                .nextStop(stop)
                .build();
    }

    // Entity → Response
    public static BusResponse toResponse(Bus bus) {
        return BusResponse.builder()
                .id(bus.getId())
                .busId(bus.getBusId()) // UUID as String
                .busNumber(bus.getBusNumber())    // Human readable number
                .busType(String.valueOf(bus.getBusType()))
                .capacity(bus.getCapacity())
                .operationalStatus(String.valueOf(bus.getOperationalStatus()))
                .occupancyStatus(String.valueOf(bus.getOccupancyStatus()))
                .currentLat(bus.getCurrentLat())
                .currentLon(bus.getCurrentLon())
                .routeId(bus.getRoute() != null ? bus.getRoute().getRouteId().toString() : null)
                .nextStopId(bus.getNextStop() != null ? bus.getNextStop().getStopId().toString() : null)
                .build();
    }
}
package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.request.StopRequest;
import com.smartRahi.SmartRahi.DTO.response.StopResponse;
import com.smartRahi.SmartRahi.Entity.City;
import com.smartRahi.SmartRahi.Entity.Stop;

import java.util.UUID;

public class StopMapper {

    public static Stop toEntity(StopRequest req, City city) {
        if (req == null) {
            return null;
        }
        return Stop.builder()
                .stopId(req.getStopId())
                .stopName(req.getStopName())
                .stopHeadsign(req.getStopHeadsign())
                .stopLat(req.getStopLat())
                .stopLon(req.getStopLon())
                .city(city)
                .build();
    }

    public static StopResponse toResponse(Stop stop) {
        return StopResponse.builder()
                .id(stop.getId())
                .stopId(stop.getStopId())
                .stopName(stop.getStopName())
                .stopHeadsign(stop.getStopHeadsign())
                .stopLat(stop.getStopLat())
                .stopLon(stop.getStopLon())
                .build();
    }
}
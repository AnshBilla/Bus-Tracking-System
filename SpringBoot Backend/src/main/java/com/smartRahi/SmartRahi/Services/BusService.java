package com.smartRahi.SmartRahi.Services;


import com.smartRahi.SmartRahi.DTO.request.BusRequest;
import com.smartRahi.SmartRahi.DTO.response.BusResponse;

import java.util.List;
import java.util.UUID;

public interface BusService {
    BusResponse createBus(BusRequest request);
    BusResponse getBusById(String busId);
    List<BusResponse> getAllBuses();
    BusResponse updateBus(String busId, BusRequest request);
    void deleteBus(String busId);
}
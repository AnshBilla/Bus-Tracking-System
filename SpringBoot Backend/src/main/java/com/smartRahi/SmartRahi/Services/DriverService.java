package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.DriverRequest;
import com.smartRahi.SmartRahi.DTO.request.StartTripRequest;
import com.smartRahi.SmartRahi.DTO.response.DriverResponse;

import java.util.List;
import java.util.UUID;

public interface DriverService {
    DriverResponse createDriver(DriverRequest request);
    DriverResponse getDriverById(UUID driverId);
    List<DriverResponse> getAllDrivers();
    DriverResponse updateDriver(UUID driverId, DriverRequest request);
    void deleteDriver(UUID driverId);

    void startTrip(String driverUsername, StartTripRequest request);

    /**
     * Ek 'ACTIVE' trip ko 'COMPLETED' set karta hai
     */
    void endTrip(String driverUsername);
}
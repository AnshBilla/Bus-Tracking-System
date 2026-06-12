package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.response.DriverResponse;
import com.smartRahi.SmartRahi.Entity.Driver;
import com.smartRahi.SmartRahi.Entity.DriverProfile;
import com.smartRahi.SmartRahi.Entity.User;

public class DriverMapper {

    /**
     * Converts linked entities into a single, flat DriverResponse.
     * This is the "Single Source of Truth" (SSOT) method.
     *
     * @param driver  The operational entity (for bus assignment)
     * @param user    The identity entity (for name, phone)
     * @param profile The credential entity (for employeeId, license)
     * @return A complete DriverResponse DTO
     */
    public static DriverResponse toResponse(Driver driver, User user, DriverProfile profile) {
        if (driver == null || user == null || profile == null) {
            return null;
        }

        return DriverResponse.builder()
                // From Driver entity (Operation)
                .driverId(driver.getId().toString())
                .assignedBusId(driver.getAssignedBus() != null ? driver.getAssignedBus().getBusId() : null)

                // From User entity (Identity)
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())

                // From DriverProfile entity (Credentials)
                .employeeId(profile.getEmployeeId())
                .licenseNumber(profile.getDrivingLicense())
                .build();
    }

    /**
     * Overloaded method for just the Driver entity.
     * WARNING: This will return a partial response.
     * Only use this if you CANNOT get the User and Profile.
     */
    public static DriverResponse toResponse(Driver driver) {
        if (driver == null) {
            return null;
        }

        return DriverResponse.builder()
                .driverId(driver.getId().toString())
                .assignedBusId(driver.getAssignedBus() != null ? driver.getAssignedBus().getBusId() : null)
                // Note: fullName, phoneNumber, employeeId, licenseNumber will be NULL
                .build();
    }
}
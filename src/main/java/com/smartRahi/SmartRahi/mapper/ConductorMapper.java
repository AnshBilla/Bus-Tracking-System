package com.smartRahi.SmartRahi.mapper;

import com.smartRahi.SmartRahi.DTO.response.ConductorResponse;
import com.smartRahi.SmartRahi.Entity.Conductor;
import com.smartRahi.SmartRahi.Entity.ConductorProfile;
import com.smartRahi.SmartRahi.Entity.User;

public class ConductorMapper {

    /**
     * Converts linked entities into a single, flat ConductorResponse.
     * This is the "Single Source of Truth" (SSOT) method.
     *
     * @param conductor The operational entity (for bus assignment)
     * @param user      The identity entity (for name, phone)
     * @param profile   The credential entity (for employeeId)
     * @return A complete ConductorResponse DTO
     */
    public static ConductorResponse toResponse(Conductor conductor, User user, ConductorProfile profile) {
        if (conductor == null || user == null || profile == null) {
            return null;
        }

        return ConductorResponse.builder()
                // From Conductor entity (Operation)
                .conductorId(conductor.getId().toString())
                .assignedBusId(conductor.getAssignedBus() != null ? conductor.getAssignedBus().getBusId() : null)

                // From User entity (Identity)
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())

                // From ConductorProfile entity (Credentials)
                .employeeId(profile.getEmployeeId())
                .build();
    }

}
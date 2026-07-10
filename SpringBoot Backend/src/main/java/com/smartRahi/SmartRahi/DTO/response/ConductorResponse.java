package com.smartRahi.SmartRahi.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns conductor details by combining 3 entities
public class ConductorResponse {

    // From Conductor (Operational) entity
    private String conductorId; // The UUID of the operational entity
    private String assignedBusId;

    // From ConductorProfile (Credential) entity
    private String employeeId;

    // From User (Identity) entity
    private String fullName;
    private String phoneNumber;
}
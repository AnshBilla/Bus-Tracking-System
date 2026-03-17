package com.smartRahi.SmartRahi.DTO.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Returns driver details.
public class DriverResponse {
    private String driverId;
    private String employeeId;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private String assignedBusId;

}
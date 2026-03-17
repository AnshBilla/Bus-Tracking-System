package com.smartRahi.SmartRahi.DTO.request;

import com.smartRahi.SmartRahi.enums.Role;
import lombok.Data;
import jakarta.validation.constraints.*;
/// Used when a new user registers.
@Data
public class StaffRegisterRequest {
    @NotBlank(message = "Username is required")
    private String username;
    @Email(message = "Email shoudl be valid")
    @NotBlank(message ="Email is required")
    private String email;
    @NotBlank(message = "Mobile Number is required") private String phone;
    @NotBlank(message = "Password is required") @Size(min=8,message = "Password must be atleast 8 characters")
    private String password;
    @NotNull(message = "Role is required") private Role role;
    private String fullName;
    private String address;



    private String drivingLicense;


    private String aadhar;


    private String employeeId;


    // driver

    private Integer experienceYears;
    private String adminSecret;

}
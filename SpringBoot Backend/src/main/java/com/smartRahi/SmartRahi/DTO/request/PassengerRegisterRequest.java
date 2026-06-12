package com.smartRahi.SmartRahi.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PassengerRegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message ="Email is required")
    private String email;

    @NotBlank(message = "Mobile Number is required")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min=8, message = "Password must be at least 8 characters")
    private String password;

    private String fullName;
    private String address;

    // Note: No fields for Aadhar, License, EmployeeID, or AdminSecret
}
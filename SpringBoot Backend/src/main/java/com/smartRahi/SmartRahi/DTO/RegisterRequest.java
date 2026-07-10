package com.smartRahi.SmartRahi.DTO;

import com.smartRahi.SmartRahi.enums.Role;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    @NotBlank @Size(min=6)
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one letter, one number, and one special character"
    )

    private String password;
    @NotNull private Role role;
    private String fullName;
    private String address;

    // driver
    private String drivingLicense;
    private String aadhar;
    private Integer experienceYears;

    // conductor
    private String employeeId;
}
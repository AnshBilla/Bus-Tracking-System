package com.smartRahi.SmartRahi.DTO.request;
/// dto are data transfer objects
/// Request dtos--> Used when the client sends data to the server
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AuthRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
///AuthRequest ka kaam hai:
///
/// User ka login input capture karna (username/email + password)
///
/// Service layer tak bhejna
///
/// Jiske basis pe authentication hogi aur JWT generate hoga
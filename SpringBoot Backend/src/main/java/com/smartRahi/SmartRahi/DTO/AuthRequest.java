package com.smartRahi.SmartRahi.DTO;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AuthRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
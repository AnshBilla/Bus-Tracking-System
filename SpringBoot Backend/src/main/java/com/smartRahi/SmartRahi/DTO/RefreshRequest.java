package com.smartRahi.SmartRahi.DTO;


import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RefreshRequest {
    @NotBlank private String refreshToken;
}
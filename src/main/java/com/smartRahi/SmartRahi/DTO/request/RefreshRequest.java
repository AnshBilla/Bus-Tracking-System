package com.smartRahi.SmartRahi.DTO.request;


import lombok.Data;
import jakarta.validation.constraints.*;
/// Used to refresh an expired/expiring JWT token.
@Data
public class RefreshRequest {
    @NotBlank private String refreshToken;
}
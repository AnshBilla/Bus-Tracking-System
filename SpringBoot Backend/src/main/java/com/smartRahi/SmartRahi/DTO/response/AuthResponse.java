package com.smartRahi.SmartRahi.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
/// Used when the server sends data back to the client
@Data
@AllArgsConstructor
/// Sent after login/register/refresh.
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}

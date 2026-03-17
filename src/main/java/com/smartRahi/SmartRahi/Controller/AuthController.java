package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.AuthRequest;
import com.smartRahi.SmartRahi.DTO.request.PassengerRegisterRequest;
import com.smartRahi.SmartRahi.DTO.response.AuthResponse;
import com.smartRahi.SmartRahi.DTO.request.RefreshRequest;
import com.smartRahi.SmartRahi.DTO.request.StaffRegisterRequest;
import com.smartRahi.SmartRahi.Services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.refresh-token-ms}")
    private long refreshTtlMs; ///the refresh token’s expiration time in milliseconds

    @PostMapping("/register/passenger")
    public ResponseEntity<AuthResponse> registerPassenger(
            @Validated @RequestBody PassengerRegisterRequest req) {

        var resp = authService.registerPassenger(req, refreshTtlMs);
        return ResponseEntity.ok(resp);
    }

    /**
     * Secured endpoint for an ADMIN to register new staff
     * (drivers, operators, or other admins).
     */
    @PostMapping("/register/staff")
    // Merge conflict fix: Annotation ko active rakha gaya hai
    @PreAuthorize("hasRole('ADMIN')") // ⭐️ SECURED
    public ResponseEntity<AuthResponse> registerStaff(
            @Validated @RequestBody StaffRegisterRequest req) {

        var resp = authService.registerStaff(req, refreshTtlMs);
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/login")   /// to handle the login request
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        var resp = authService.authenticate(req, refreshTtlMs);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh-token") ///  // Refreshes the user's authentication token and returns the new token
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshRequest req) {
        var resp = authService.refresh(req, refreshTtlMs);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")///  handle the logout request
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
        return ResponseEntity.ok().build();
    }

    // Guest creation endpoint (for villagers: "Continue as Guest")
    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> guest() {
        var resp = authService.createGuest(refreshTtlMs);
        return ResponseEntity.ok(resp);
    }

}
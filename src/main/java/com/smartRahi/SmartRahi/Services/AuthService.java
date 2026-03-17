package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.AuthRequest;
import com.smartRahi.SmartRahi.DTO.request.PassengerRegisterRequest;
import com.smartRahi.SmartRahi.DTO.response.AuthResponse;
import com.smartRahi.SmartRahi.DTO.request.RefreshRequest;
import com.smartRahi.SmartRahi.DTO.request.StaffRegisterRequest;
import com.smartRahi.SmartRahi.Entity.ConductorProfile;
import com.smartRahi.SmartRahi.Entity.DriverProfile;
import com.smartRahi.SmartRahi.Repository.*;
import com.smartRahi.SmartRahi.enums.Role;
import com.smartRahi.SmartRahi.Entity.User;
import com.smartRahi.SmartRahi.Security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final Environment env;

    private final DriverRepository driverRepo;
    private final ConductorRepository conductorRepo;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final DriverProfileRepository driverProfile;
    private final DriverProfileRepository driverProfileRepository;
    private final ConductorProfileRepository conductorProfileRepository;

    @Transactional
    public AuthResponse registerPassenger(PassengerRegisterRequest req, long refreshTtlMs) {

        // 1. Duplicate checks
        if (userRepository.existsByUsername(req.getUsername()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        if (userRepository.existsByPhoneNumber(req.getPhone()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is already taken");

        // 2. Create user entity
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.passenger) // ⭐️ Role is hardcoded to passenger
                .createdAt(LocalDateTime.now())
                .lastLoggedIn(LocalDateTime.now())
                .fullName(req.getFullName())
                .address(req.getAddress())
                .build();

        user = userRepository.save(user);

        // 3. (No profile creation needed for passenger)

        // 4. Generate tokens
        String access = jwtService.generateAccessToken(user);
        var refresh = refreshTokenService.createRefreshToken(user, refreshTtlMs);

        return new AuthResponse(access, refresh.getToken(), "Bearer");
    }

    /**
     * Registers a new Staff member (Driver, Operator, Admin).
     * This logic is moved from your old register method.
     */
    @Transactional
    public AuthResponse registerStaff(StaffRegisterRequest req, long refreshTtlMs) {

        // 1. Duplicate checks
        if (userRepository.existsByUsername(req.getUsername()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        if (userRepository.existsByPhoneNumber(req.getPhone()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is already taken");

        // 2. Role-based validation (This is your old logic)
        switch (req.getRole()) {
            case driver:
                if (req.getAadhar() == null || req.getDrivingLicense() == null || req.getEmployeeId() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Driver must provide Aadhar, Driving License, and Employee ID");
                break;
            case operator:
                if (req.getAadhar() == null || req.getEmployeeId() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Operator must provide Aadhar and Employee ID");
                break;
            case admin:
                if (req.getAadhar() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Admin must provide Aadhar");
                String secret = env.getProperty("ADMIN_SECRET");
                if (req.getAdminSecret() == null || !req.getAdminSecret().equals(secret))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin secret");
                break;
            case passenger: // Fall-through
            case guest:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot register passenger/guest via this endpoint");
            default:
                break;
        }

        // 3. Create user entity
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole()) // ⭐️ Role comes from the request
                .createdAt(LocalDateTime.now())
                .lastLoggedIn(LocalDateTime.now())
                .fullName(req.getFullName())
                .address(req.getAddress())
                .build();
        user = userRepository.save(user);

        // 4. Role-specific profile creation
        if (req.getRole() == Role.driver) {
            DriverProfile dp = DriverProfile.builder()
                    .user(user)
                    .aadhar(req.getAadhar())
                    .drivingLicense(req.getDrivingLicense())
                    .employeeId(req.getEmployeeId())
                    .experienceYears(req.getExperienceYears())
                    .build();
            driverProfileRepository.save(dp);

        } else if (req.getRole() == Role.operator) {
            ConductorProfile cp = ConductorProfile.builder()
                    .user(user)
                    .employeeId(req.getEmployeeId())
                    .aadhar(req.getAadhar())
                    .build();
            conductorProfileRepository.save(cp);
        }

        // 5. Generate tokens
        String access = jwtService.generateAccessToken(user);
        var refresh = refreshTokenService.createRefreshToken(user, refreshTtlMs);

        return new AuthResponse(access, refresh.getToken(), "Bearer");
    }

    public AuthResponse authenticate(AuthRequest req, long refreshTtlMs) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        String access = jwtService.generateAccessToken(user);
        var refreshEntity = refreshTokenService.createRefreshToken(user, refreshTtlMs);

        return new AuthResponse(access, refreshEntity.getToken(), "Bearer");
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req, long refreshTtlMs) {
        var found = refreshTokenService.findByToken(req.getRefreshToken());

        if (found == null || !refreshTokenService.isValid(found)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // Revoke old token
        refreshTokenService.revoke(found);

        // Generate new tokens
        User user = found.getUser();
        String access = jwtService.generateAccessToken(user);
        var newRefresh = refreshTokenService.createRefreshToken(user, refreshTtlMs);

        return new AuthResponse(access, newRefresh.getToken(), "Bearer");
    }


    public void logout(RefreshRequest req) {/// revoke refresh token.
        var found = refreshTokenService.findByToken(req.getRefreshToken());
        if (found == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token not found");
        }
        refreshTokenService.revoke(found);
    }

    // create guest account (no password) -> returns tokens
    public AuthResponse createGuest(long refreshTtlMs) {
        // Ensure unique guest username
        String username;
        do {
            username = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.existsByUsername(username));

        User guest = User.builder()
                .username(username)
                .role(Role.guest)
                .build();
        guest = userRepository.save(guest);
        String access = jwtService.generateGuestAccessToken(guest);
        var refresh = refreshTokenService.createRefreshToken(guest, refreshTtlMs);
        return new AuthResponse(access, refresh.getToken(),"Bearer");
    }

}

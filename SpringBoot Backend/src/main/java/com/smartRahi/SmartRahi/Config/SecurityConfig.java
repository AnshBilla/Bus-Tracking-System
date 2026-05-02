package com.smartRahi.SmartRahi.Config;

import com.smartRahi.SmartRahi.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// @PreAuthorize ke liye import
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity // @PreAuthorize annotations ko enable karta hai
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // Your React Vite port
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ----------------------------------------------------------------
                        // PUBLIC ENDPOINTS (Koi token zaroori nahi)
                        // ----------------------------------------------------------------
                        .requestMatchers("/api/auth/**").permitAll() // Login, Register, Refresh, Guest
                        .requestMatchers("/healthz").permitAll()     // Health check
                        // Swagger (API Docs) ke liye public access
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // ----------------------------------------------------------------
                        // USER-SPECIFIC ENDPOINTS
                        // ----------------------------------------------------------------
                        .requestMatchers("/api/passenger/**").hasAnyRole("PASSENGER", "GUEST")
                        .requestMatchers("/api/driver/**").hasRole("DRIVER")
                        .requestMatchers("/api/conductor/**").hasRole("operator") // Role enum ke hisaab se
                        .requestMatchers("/api/staff/**").hasAnyRole("DRIVER", "operator")
                        .requestMatchers("/api/bus/realtime/**").hasAnyRole("DRIVER", "operator")

                        // ----------------------------------------------------------------
                        // ADMIN-ONLY ENDPOINTS (Infrastructure Management)
                        // ----------------------------------------------------------------

                        // Merge conflict fix: Sirf ADMIN ko access dena behtar security hai
                        .requestMatchers("/api/buses/**").hasRole("ADMIN")
                        .requestMatchers("/api/vehicle/update/**").permitAll() // GPS module ke liye access
                        .requestMatchers("/api/routes/**").hasRole("ADMIN")
                        .requestMatchers("/api/stops/**").hasRole("ADMIN")
                        .requestMatchers("/api/trips/**").hasRole("ADMIN")
                        .requestMatchers("/api/route-stops/**").hasRole("ADMIN")
                        .requestMatchers("/api/drivers/**").hasRole("ADMIN")
                        .requestMatchers("/api/conductors/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ----------------------------------------------------------------
                        // DEFAULT: Baaki sabhi requests ke liye authentication zaroori hai
                        // ----------------------------------------------------------------
                        .anyRequest().authenticated()
                )
                // JWT filter ko add karein
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
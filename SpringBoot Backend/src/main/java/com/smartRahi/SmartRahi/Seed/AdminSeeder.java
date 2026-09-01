package com.smartRahi.SmartRahi.Seed;

import com.smartRahi.SmartRahi.Entity.User;
import com.smartRahi.SmartRahi.Repository.UserRepository;
import com.smartRahi.SmartRahi.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Real projects need a default admin to start with
        if (!userRepository.existsByUsername("admin")) {
            log.info("No Master Admin found. Creating default admin account...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@smartrahi.com")
                    .phoneNumber("9999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.admin)
                    .createdAt(LocalDateTime.now())
                    .lastLoggedIn(LocalDateTime.now())
                    .fullName("System Administrator")
                    .build();
            userRepository.save(admin);
            log.info("Default admin created successfully! Username: admin | Password: admin123");
        }
    }
}

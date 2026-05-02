package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.Entity.RefreshToken;
import com.smartRahi.SmartRahi.Entity.User;
import com.smartRahi.SmartRahi.Repository.RefreshTokenRepository;
import com.smartRahi.SmartRahi.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshToken createRefreshToken(User user, long ttlMs) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString() + "." + jwtService.generateRefreshToken(user))
                .user(user)
                .expiryDate(Instant.now().plusMillis(ttlMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryDate().isAfter(Instant.now());
    }

    public RefreshToken findByToken(String tokenStr) {
        return refreshTokenRepository.findByToken(tokenStr).orElse(null);
    }

    public void revoke(RefreshToken token) {
        if (token == null) return;
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeAllForUser(User user) {
        var list = refreshTokenRepository.findAllByUser(user);
        list.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(list);
    }
}
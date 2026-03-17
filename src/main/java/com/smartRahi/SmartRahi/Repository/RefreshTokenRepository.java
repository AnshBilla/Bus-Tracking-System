package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.RefreshToken;
import com.smartRahi.SmartRahi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(User user);
    void deleteAllByUser(User user);
}

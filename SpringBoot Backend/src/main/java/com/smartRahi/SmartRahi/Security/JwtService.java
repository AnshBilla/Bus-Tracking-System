package com.smartRahi.SmartRahi.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
// Service for generating, parsing, and validating JWT access and refresh tokens using a secret key and configurable expiration times.
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-ms}")
    private long accessTokenMs;

    @Value("${app.jwt.refresh-token-ms}")
    private long refreshTokenMs;

    // Generate the signing key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes()); // secret must be at least 256-bit
    }

    // Extract all claims from a token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Generic claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration != null && expiration.before(new Date());
    }

    // Validate token against UserDetails
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Generate standard access token
    public String generateAccessToken(UserDetails user) {
        return buildToken(Map.of("roles", user.getAuthorities()), user.getUsername(), accessTokenMs);
    }

    // Generate guest access token (never expires)
    public String generateGuestAccessToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", "GUEST")
                .setIssuedAt(Date.from(now))
                // ❌ No expiration set => token never expires
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate refresh token
    public String generateRefreshToken(UserDetails user) {
        return buildToken(Map.of(), user.getUsername(), refreshTokenMs);
    }

    // Build a token with extra claims and expiration
    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}

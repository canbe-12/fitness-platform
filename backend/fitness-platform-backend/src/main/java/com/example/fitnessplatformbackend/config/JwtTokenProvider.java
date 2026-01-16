package com.example.fitnessplatformbackend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtTokenProvider {
    private final SecretKey key;
    private final long expireSeconds;

    public JwtTokenProvider(String secret, long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        String sub = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(sub);
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}

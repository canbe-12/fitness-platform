package com.example.fitnessplatformbackend.dto.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn
) {}

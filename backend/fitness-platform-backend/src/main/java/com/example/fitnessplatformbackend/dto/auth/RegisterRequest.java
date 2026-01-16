package com.example.fitnessplatformbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password
) {}

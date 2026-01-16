package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.auth.LoginRequest;
import com.example.fitnessplatformbackend.dto.auth.LoginResponse;
import com.example.fitnessplatformbackend.dto.auth.RegisterRequest;
import com.example.fitnessplatformbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req.username(), req.password());
        return ApiResponse.ok(null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.username(), req.password());
        return ApiResponse.ok(new LoginResponse(token, auth.expiresInSeconds()));
    }
}

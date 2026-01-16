package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.user.*;
import com.example.fitnessplatformbackend.security.SecurityUtils;
import com.example.fitnessplatformbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me() {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(userService.getMe(uid));
    }

    @PutMapping("/me")
    public ApiResponse<UserMeResponse> update(@RequestBody UserUpdateRequest req) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(userService.updateMe(uid, req));
    }
}

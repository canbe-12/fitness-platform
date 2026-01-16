package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.body.*;
import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.security.SecurityUtils;
import com.example.fitnessplatformbackend.service.BodyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/body")
@RequiredArgsConstructor
public class BodyController {
    private final BodyService bodyService;

    @PutMapping("/weight")
    public ApiResponse<Void> upsertWeight(@RequestParam LocalDate date, @RequestBody WeightUpsertRequest req) {
        Long uid = SecurityUtils.currentUserId();
        bodyService.upsertWeight(uid, date, req);
        return ApiResponse.ok(null);
    }

    @GetMapping("/weight/trend")
    public ApiResponse<List<WeightTrendPoint>> trend(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(bodyService.trend(uid, from, to));
    }
}

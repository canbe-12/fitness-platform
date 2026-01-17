package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.plan.PlanResponse;
import com.example.fitnessplatformbackend.dto.plan.PlanUpsertRequest;
import com.example.fitnessplatformbackend.service.PlanService;
import com.example.fitnessplatformbackend.service.SecurityCurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final SecurityCurrentUserService currentUser;

    @GetMapping("/current")
    public ApiResponse<PlanResponse> getCurrent() {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(planService.getCurrent(userId));
    }

    @PutMapping("/current")
    public ApiResponse<PlanResponse> upsert(@Valid @RequestBody PlanUpsertRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(planService.upsertCurrent(userId, req));
    }
}

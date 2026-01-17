package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.diet.*;
import com.example.fitnessplatformbackend.service.DietLogService;
import com.example.fitnessplatformbackend.service.SecurityCurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet-logs")
@RequiredArgsConstructor
public class DietLogController {

    private final DietLogService dietLogService;
    private final SecurityCurrentUserService currentUser;

    @PostMapping
    public ApiResponse<DietLogCreateResponse> create(@Valid @RequestBody DietLogCreateRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.createOrGet(userId, req));
    }

    @GetMapping("/day")
    public ApiResponse<DietLogDayResponse> day(@RequestParam("date") LocalDate date) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.getDay(userId, date));
    }

    @GetMapping("/range")
    public ApiResponse<List<DietLogRangeDaySummary>> range(@RequestParam("start") LocalDate start,
                                              @RequestParam("end") LocalDate end) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.getRange(userId, start, end));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = currentUser.getCurrentUserId();
        dietLogService.delete(userId, id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<DietLogDetailResponse> detail(@PathVariable Long id) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.getDetail(userId, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<DietLogDetailResponse> update(@PathVariable Long id, @Valid @RequestBody DietLogUpdateRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.updateNote(userId, id, req.getNote()));
    }

    @GetMapping("/nutrition/day")
    public ApiResponse<NutritionSnapshotDto> nutritionDay(@RequestParam("date") LocalDate date) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.getNutritionDay(userId, date));
    }

    @GetMapping("/nutrition/range")
    public ApiResponse<List<DietNutritionRangeDayResponse>> nutritionRange(@RequestParam("start") LocalDate start,
                                                              @RequestParam("end") LocalDate end) {
        Long userId = currentUser.getCurrentUserId();
        return ApiResponse.ok(dietLogService.getNutritionRange(userId, start, end));
    }



}

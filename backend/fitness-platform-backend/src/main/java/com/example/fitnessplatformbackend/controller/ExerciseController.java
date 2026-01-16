package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.exercise.*;
import com.example.fitnessplatformbackend.service.ExerciseService;
import com.example.fitnessplatformbackend.service.ExerciseLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final ExerciseLogService exerciseLogService;

    @GetMapping
    public ApiResponse<Page<ExerciseResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(exerciseService.search(keyword, muscleGroup, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExerciseResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(exerciseService.detail(id));
    }

    // 趋势：maxWeight per day（你也可以改成 avg/reps 等）
    @GetMapping("/{id}/trend")
    public ApiResponse<List<ExerciseTrendPoint>> trend(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.ok(exerciseLogService.trendMaxWeight(id, from, to));
    }
}

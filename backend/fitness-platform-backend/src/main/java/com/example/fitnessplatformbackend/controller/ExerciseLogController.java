package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.exercise.ExerciseTrendPoint;
import com.example.fitnessplatformbackend.dto.exerciselog.ExerciseLogResponse;
import com.example.fitnessplatformbackend.dto.exerciselog.ExerciseLogSubmitRequest;
import com.example.fitnessplatformbackend.security.SecurityUtils;
import com.example.fitnessplatformbackend.service.ExerciseLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExerciseLogController {
    private final ExerciseLogService exerciseLogService;

    // ✅ 推荐新接口
    @PostMapping("/api/exercise-logs")
    public ApiResponse<ExerciseLogResponse> submit(@RequestBody ExerciseLogSubmitRequest req) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(exerciseLogService.submit(uid, req));
    }

    // ✅ 兼容你旧接口 /api/exercise/log
    @PostMapping("/api/exercise/log")
    public ApiResponse<ExerciseLogResponse> submitCompat(@RequestBody ExerciseLogSubmitRequest req) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(exerciseLogService.submit(uid, req));
    }

    @GetMapping("/api/exercise-logs")
    public ApiResponse<List<ExerciseLogResponse>> listByDate(@RequestParam LocalDate date) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(exerciseLogService.listByDate(uid, date));
    }

    @GetMapping("/api/exercise-logs/range")
    public ApiResponse<List<ExerciseLogResponse>> listRange(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(exerciseLogService.listRange(uid, from, to));
    }

    @DeleteMapping("/api/exercise-logs/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long uid = SecurityUtils.currentUserId();
        exerciseLogService.delete(uid, id);
        return ApiResponse.ok(null);
    }
}

package com.example.fitnessplatformbackend.controller;

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
    public DietLogCreateResponse create(@Valid @RequestBody DietLogCreateRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.createOrGet(userId, req);
    }

    @GetMapping("/day")
    public DietLogDayResponse day(@RequestParam("date") LocalDate date) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.getDay(userId, date);
    }

    @GetMapping("/range")
    public List<DietLogRangeDaySummary> range(@RequestParam("start") LocalDate start,
                                              @RequestParam("end") LocalDate end) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.getRange(userId, start, end);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long userId = currentUser.getCurrentUserId();
        dietLogService.delete(userId, id);
    }

    @GetMapping("/{id}")
    public DietLogDetailResponse detail(@PathVariable Long id) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.getDetail(userId, id);
    }

    @PutMapping("/{id}")
    public DietLogDetailResponse update(@PathVariable Long id, @Valid @RequestBody DietLogUpdateRequest req) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.updateNote(userId, id, req.getNote());
    }

    @GetMapping("/nutrition/day")
    public NutritionSnapshotDto nutritionDay(@RequestParam("date") LocalDate date) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.getNutritionDay(userId, date);
    }

    @GetMapping("/nutrition/range")
    public List<DietNutritionRangeDayResponse> nutritionRange(@RequestParam("start") LocalDate start,
                                                              @RequestParam("end") LocalDate end) {
        Long userId = currentUser.getCurrentUserId();
        return dietLogService.getNutritionRange(userId, start, end);
    }



}

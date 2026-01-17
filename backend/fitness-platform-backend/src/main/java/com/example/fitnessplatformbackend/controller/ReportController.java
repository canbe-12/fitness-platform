package com.example.fitnessplatformbackend.controller;

import com.example.fitnessplatformbackend.dto.common.ApiResponse;
import com.example.fitnessplatformbackend.dto.report.ComprehensiveReportResponse;
import com.example.fitnessplatformbackend.dto.report.WeeklyReportResponse;
import com.example.fitnessplatformbackend.security.SecurityUtils;
import com.example.fitnessplatformbackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/weekly")
    public ApiResponse<WeeklyReportResponse> weekly(@RequestParam(required = false) LocalDate weekStart) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(reportService.weekly(uid, weekStart));
    }

    @GetMapping("/comprehensive")
    public ApiResponse<ComprehensiveReportResponse> comprehensive(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        Long uid = SecurityUtils.currentUserId();
        return ApiResponse.ok(reportService.comprehensive(uid, start, end));
    }
}

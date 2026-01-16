package com.example.fitnessplatformbackend.dto.report;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class WeeklyReportResponse {
    private String weekStart;
    private String weekEnd;
    private long workoutDays;
    private BigDecimal weightStartKg;
    private BigDecimal weightEndKg;
}

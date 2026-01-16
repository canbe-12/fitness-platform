package com.example.fitnessplatformbackend.dto.plan;

import com.example.fitnessplatformbackend.entity.enums.PlanTarget;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PlanResponse {
    private PlanTarget target;
    private Integer weeklyWorkoutDays;
    private Integer dailyKcalTarget;
    private BigDecimal proteinTargetG;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

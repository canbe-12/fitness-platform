package com.example.fitnessplatformbackend.dto.plan;

import com.example.fitnessplatformbackend.entity.enums.PlanTarget;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanUpsertRequest {
    @NotNull
    private PlanTarget target;

    @NotNull @Min(0)
    private Integer weeklyWorkoutDays;

    @NotNull @Min(0)
    private Integer dailyKcalTarget;

    @NotNull
    private BigDecimal proteinTargetG;
}

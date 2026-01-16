package com.example.fitnessplatformbackend.dto.exerciselog;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExerciseLogSubmitRequest {
    private Long exerciseId;
    private LocalDate plannedDate;

    // 允许补录：若 plannedDate < today，可在服务端自动 isBackfill=true
    private BigDecimal actualWeight;
    private Integer actualReps;

    private BigDecimal plannedWeight;
    private Integer plannedReps;

    private String clientRequestId;
    private String notes;
}

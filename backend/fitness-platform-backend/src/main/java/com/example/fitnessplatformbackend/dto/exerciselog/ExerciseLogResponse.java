package com.example.fitnessplatformbackend.dto.exerciselog;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class ExerciseLogResponse {
    private Long id;
    private Long exerciseId;
    private LocalDate plannedDate;
    private LocalDateTime loggedAt;
    private Boolean isBackfill;

    private BigDecimal actualWeight;
    private Integer actualReps;

    private String notes;
}

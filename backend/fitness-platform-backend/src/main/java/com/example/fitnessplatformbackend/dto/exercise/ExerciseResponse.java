package com.example.fitnessplatformbackend.dto.exercise;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class ExerciseResponse {
    private Long id;
    private String name;
    private String muscleGroup;
    private String equipment;
    private BigDecimal defaultTargetWeight;
    private Integer defaultTargetReps;
}

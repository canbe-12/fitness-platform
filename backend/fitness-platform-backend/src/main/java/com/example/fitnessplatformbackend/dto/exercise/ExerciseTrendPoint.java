package com.example.fitnessplatformbackend.dto.exercise;

import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class ExerciseTrendPoint {
    private String date; // yyyy-MM-dd
    private BigDecimal maxWeight;
}

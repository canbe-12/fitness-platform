package com.example.fitnessplatformbackend.dto.body;

import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class WeightTrendPoint {
    private String date; // yyyy-MM-dd
    private BigDecimal weightKg;
}

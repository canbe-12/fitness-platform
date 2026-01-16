package com.example.fitnessplatformbackend.dto.body;

import lombok.*;
import java.math.BigDecimal;

@Data
public class WeightUpsertRequest {
    private BigDecimal weightKg;
}

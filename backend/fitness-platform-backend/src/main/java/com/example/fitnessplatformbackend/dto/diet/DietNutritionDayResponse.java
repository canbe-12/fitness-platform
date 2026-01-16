package com.example.fitnessplatformbackend.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DietNutritionDayResponse {
    private LocalDate date;
    private NutritionSnapshotDto total;
}

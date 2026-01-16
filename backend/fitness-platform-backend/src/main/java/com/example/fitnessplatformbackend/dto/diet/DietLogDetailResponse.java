package com.example.fitnessplatformbackend.dto.diet;

import com.example.fitnessplatformbackend.entity.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DietLogDetailResponse {
    private Long id;
    private LocalDate date;
    private MealType mealType;
    private String note;
    private NutritionSnapshotDto nutritionSnapshot;
    private List<DietLogItemDto> items;
}

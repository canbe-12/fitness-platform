package com.example.fitnessplatformbackend.dto.diet;

import com.example.fitnessplatformbackend.entity.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DietLogDayResponse {
    private LocalDate date;
    private List<MealSummary> meals;
    private NutritionSnapshotDto dayTotal;

    @Data
    @AllArgsConstructor
    public static class MealSummary {
        private Long id;
        private MealType mealType;
        private Integer kcal;
        private java.math.BigDecimal proteinG;
        private java.math.BigDecimal carbG;
        private java.math.BigDecimal fatG;
    }
}

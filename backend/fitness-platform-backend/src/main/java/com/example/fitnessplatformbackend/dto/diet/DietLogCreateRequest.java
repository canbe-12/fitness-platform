package com.example.fitnessplatformbackend.dto.diet;

import com.example.fitnessplatformbackend.entity.enums.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DietLogCreateRequest {
    @NotBlank
    private String clientRequestId;

    @NotNull
    private LocalDate date; // 允许补录

    @NotNull
    private MealType mealType;

    @Valid
    @NotNull
    private List<DietLogItemDto> items;

    // 可选：前端直接传“快照”；若不传，则后端按 items 汇总计算
    @Valid
    private NutritionSnapshotDto nutritionSnapshot;

    private String note;
}

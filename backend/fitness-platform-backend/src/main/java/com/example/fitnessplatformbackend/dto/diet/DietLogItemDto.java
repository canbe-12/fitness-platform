package com.example.fitnessplatformbackend.dto.diet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DietLogItemDto {
    private Long foodId;

    @NotBlank
    private String foodName;

    private String brand;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String unit;

    @NotNull @Min(0)
    private Integer kcal;

    @NotNull
    private BigDecimal proteinG;

    @NotNull
    private BigDecimal carbG;

    @NotNull
    private BigDecimal fatG;

    @NotNull
    private BigDecimal fiberG;

    @NotNull @Min(0)
    private Integer sodiumMg;
}

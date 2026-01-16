package com.example.fitnessplatformbackend.dto.food;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FoodResponse {
    private Long id;
    private String name;
    private String brand;
    private String unit;

    private Integer kcal;
    private BigDecimal proteinG;
    private BigDecimal carbG;
    private BigDecimal fatG;
    private BigDecimal fiberG;
    private Integer sodiumMg;

    private Boolean isOfficial;
}

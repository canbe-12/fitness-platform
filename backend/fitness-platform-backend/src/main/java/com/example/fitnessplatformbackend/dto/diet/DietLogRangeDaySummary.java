package com.example.fitnessplatformbackend.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DietLogRangeDaySummary {
    private LocalDate date;
    private Integer kcal;
    private BigDecimal proteinG;
    private BigDecimal carbG;
    private BigDecimal fatG;
}

package com.example.fitnessplatformbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveReportResponse {
    private String start;
    private String end;
    
    private List<DailyTrainingReport> trainingHistory;
    private List<DailyDietReport> dietHistory;
    private ReportSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrainingReport {
        private String date;
        private int durationMinutes; // Estimated: sets * 3
        private int caloriesBurned; // Estimated: volume * 0.05
        private int setCount;
        private BigDecimal volume;
        private List<String> bodyParts;
        private List<String> exerciseNames; // Top 3?
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyDietReport {
        private String date;
        private int kcal;
        private double protein;
        private double carb;
        private double fat;
        private int mealCount;
        private java.util.Map<String, Integer> mealKcalBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSummary {
        private int totalWorkouts;
        private int totalDurationMinutes;
        private int totalTrainingKcal;
        
        private int avgDailyKcalIntake;
        private double avgProtein;
        private double avgCarb;
        private double avgFat;
    }
}

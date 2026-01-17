package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.report.ComprehensiveReportResponse;
import com.example.fitnessplatformbackend.dto.report.WeeklyReportResponse;
import com.example.fitnessplatformbackend.entity.DietLogEntity;
import com.example.fitnessplatformbackend.entity.ExerciseEntity;
import com.example.fitnessplatformbackend.entity.ExerciseLogEntity;
import com.example.fitnessplatformbackend.repository.BodyMeasurementRepository;
import com.example.fitnessplatformbackend.repository.DietLogRepository;
import com.example.fitnessplatformbackend.repository.ExerciseLogRepository;
import com.example.fitnessplatformbackend.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExerciseLogRepository exerciseLogRepository;
    private final BodyMeasurementRepository bodyRepo;
    private final DietLogRepository dietLogRepository;
    private final ExerciseRepository exerciseRepository;

    public WeeklyReportResponse weekly(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        long workoutDays = exerciseLogRepository.countDistinctWorkoutDays(userId, weekStart, weekEnd);

        BigDecimal wStart = bodyRepo.findOn(userId, weekStart).map(b -> b.getWeightKg()).orElse(null);
        BigDecimal wEnd = bodyRepo.findOn(userId, weekEnd).map(b -> b.getWeightKg()).orElse(null);

        return WeeklyReportResponse.builder()
                .weekStart(weekStart.toString())
                .weekEnd(weekEnd.toString())
                .workoutDays(workoutDays)
                .weightStartKg(wStart)
                .weightEndKg(wEnd)
                .build();
    }

    public ComprehensiveReportResponse comprehensive(Long userId, LocalDate start, LocalDate end) {
        // 1. Training Data
        List<ExerciseLogEntity> logs = exerciseLogRepository.findRange(userId, start, end);
        Map<LocalDate, List<ExerciseLogEntity>> logsByDate = logs.stream()
                .collect(Collectors.groupingBy(ExerciseLogEntity::getPlannedDate));

        // Prepare exercise details
        List<Long> exerciseIds = logs.stream().map(ExerciseLogEntity::getExerciseId).distinct().toList();
        Map<Long, ExerciseEntity> exerciseMap = exerciseRepository.findAllById(exerciseIds).stream()
                .collect(Collectors.toMap(ExerciseEntity::getId, Function.identity()));

        List<ComprehensiveReportResponse.DailyTrainingReport> trainingHistory = new ArrayList<>();
        
        // Ensure we cover all dates or just dates with data? User asked for "History List".
        // Usually reports show what happened.
        
        logsByDate.forEach((date, dayLogs) -> {
            int sets = dayLogs.size();
            // Estimate duration: 3 mins per set (including rest)
            int duration = sets * 3; 
            
            BigDecimal volume = dayLogs.stream()
                    .map(l -> l.getActualWeight().multiply(new BigDecimal(l.getActualReps())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Estimate Calories: Volume * 0.05 (Rough estimate)
            // Or sets * 10 kcal
            int calories = volume.multiply(new BigDecimal("0.05")).intValue();

            List<String> bodyParts = dayLogs.stream()
                    .map(l -> exerciseMap.get(l.getExerciseId()))
                    .filter(Objects::nonNull)
                    .map(ExerciseEntity::getMuscleGroup)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<String> exNames = dayLogs.stream()
                    .map(l -> exerciseMap.get(l.getExerciseId()))
                    .filter(Objects::nonNull)
                    .map(ExerciseEntity::getName)
                    .distinct()
                    .limit(3)
                    .toList();

            trainingHistory.add(ComprehensiveReportResponse.DailyTrainingReport.builder()
                    .date(date.toString())
                    .durationMinutes(duration)
                    .caloriesBurned(calories)
                    .setCount(sets)
                    .volume(volume)
                    .bodyParts(bodyParts)
                    .exerciseNames(exNames)
                    .build());
        });
        
        trainingHistory.sort(Comparator.comparing(ComprehensiveReportResponse.DailyTrainingReport::getDate).reversed());

        // 2. Diet Data
        List<DietLogEntity> dietLogs = dietLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);
        Map<LocalDate, List<DietLogEntity>> dietByDate = dietLogs.stream()
                .collect(Collectors.groupingBy(DietLogEntity::getDate));

        List<ComprehensiveReportResponse.DailyDietReport> dietHistory = new ArrayList<>();
        
        dietByDate.forEach((date, dayLogs) -> {
            int kcal = dayLogs.stream().mapToInt(DietLogEntity::getKcal).sum();
            double protein = dayLogs.stream().map(DietLogEntity::getProteinG).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
            double carb = dayLogs.stream().map(DietLogEntity::getCarbG).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
            double fat = dayLogs.stream().map(DietLogEntity::getFatG).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();

            Map<String, Integer> mealBreakdown = dayLogs.stream()
                    .collect(Collectors.groupingBy(log -> {
                        if (log.getMealType() == null) return "其他";
                        switch (log.getMealType()) {
                            case BREAKFAST: return "早餐";
                            case LUNCH: return "午餐";
                            case DINNER: return "晚餐";
                            case SNACK: return "加餐";
                            default: return "其他";
                        }
                    }, Collectors.summingInt(DietLogEntity::getKcal)));

            dietHistory.add(ComprehensiveReportResponse.DailyDietReport.builder()
                    .date(date.toString())
                    .kcal(kcal)
                    .protein(Math.round(protein * 10.0) / 10.0)
                    .carb(Math.round(carb * 10.0) / 10.0)
                    .fat(Math.round(fat * 10.0) / 10.0)
                    .mealCount(dayLogs.size())
                    .mealKcalBreakdown(mealBreakdown)
                    .build());
        });
        
        dietHistory.sort(Comparator.comparing(ComprehensiveReportResponse.DailyDietReport::getDate).reversed());

        // 3. Summary
        int totalWorkouts = logsByDate.size();
        int totalDuration = trainingHistory.stream().mapToInt(ComprehensiveReportResponse.DailyTrainingReport::getDurationMinutes).sum();
        int totalTrainingKcal = trainingHistory.stream().mapToInt(ComprehensiveReportResponse.DailyTrainingReport::getCaloriesBurned).sum();

        int totalDietDays = dietByDate.size();
        int totalKcal = dietHistory.stream().mapToInt(ComprehensiveReportResponse.DailyDietReport::getKcal).sum();
        double totalProtein = dietHistory.stream().mapToDouble(ComprehensiveReportResponse.DailyDietReport::getProtein).sum();
        double totalCarb = dietHistory.stream().mapToDouble(ComprehensiveReportResponse.DailyDietReport::getCarb).sum();
        double totalFat = dietHistory.stream().mapToDouble(ComprehensiveReportResponse.DailyDietReport::getFat).sum();

        ComprehensiveReportResponse.ReportSummary summary = ComprehensiveReportResponse.ReportSummary.builder()
                .totalWorkouts(totalWorkouts)
                .totalDurationMinutes(totalDuration)
                .totalTrainingKcal(totalTrainingKcal)
                .avgDailyKcalIntake(totalDietDays > 0 ? totalKcal / totalDietDays : 0)
                .avgProtein(totalDietDays > 0 ? Math.round((totalProtein / totalDietDays) * 10) / 10.0 : 0)
                .avgCarb(totalDietDays > 0 ? Math.round((totalCarb / totalDietDays) * 10) / 10.0 : 0)
                .avgFat(totalDietDays > 0 ? Math.round((totalFat / totalDietDays) * 10) / 10.0 : 0)
                .build();

        return ComprehensiveReportResponse.builder()
                .start(start.toString())
                .end(end.toString())
                .trainingHistory(trainingHistory)
                .dietHistory(dietHistory)
                .summary(summary)
                .build();
    }
}

package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.report.WeeklyReportResponse;
import com.example.fitnessplatformbackend.repository.BodyMeasurementRepository;
import com.example.fitnessplatformbackend.repository.ExerciseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExerciseLogRepository exerciseLogRepository;
    private final BodyMeasurementRepository bodyRepo;

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
}

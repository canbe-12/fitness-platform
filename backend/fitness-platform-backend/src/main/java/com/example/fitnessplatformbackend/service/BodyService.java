package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.body.*;
import com.example.fitnessplatformbackend.entity.BodyMeasurementEntity;
import com.example.fitnessplatformbackend.repository.BodyMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyService {
    private final BodyMeasurementRepository repo;

    @Transactional
    public void upsertWeight(Long userId, LocalDate date, WeightUpsertRequest req) {
        BodyMeasurementEntity bm = repo.findByUserIdAndMeasureDate(userId, date).orElseGet(() -> {
            BodyMeasurementEntity newBm = new BodyMeasurementEntity();
            newBm.setCreatedAt(LocalDateTime.now());
            return newBm;
        });
        bm.setUserId(userId);
        bm.setMeasureDate(date);
        bm.setWeightKg(req.getWeightKg());
        repo.save(bm);
    }

    public List<WeightTrendPoint> trend(Long userId, LocalDate from, LocalDate to) {
        return repo.trend(userId, from, to).stream()
                .map(b -> new WeightTrendPoint(b.getMeasureDate().toString(), b.getWeightKg()))
                .toList();
    }
}

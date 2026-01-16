package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.exercise.ExerciseResponse;
import com.example.fitnessplatformbackend.entity.ExerciseEntity;
import com.example.fitnessplatformbackend.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public Page<ExerciseResponse> search(String keyword, String muscleGroup, int page, int size) {
        Page<ExerciseEntity> p = exerciseRepository.search(keyword, muscleGroup, PageRequest.of(page, size));
        return p.map(e -> ExerciseResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .muscleGroup(e.getMuscleGroup())
                .equipment(e.getEquipment())
                .defaultTargetWeight(e.getDefaultTargetWeight())
                .defaultTargetReps(e.getDefaultTargetReps())
                .build());
    }

    public ExerciseResponse detail(Long id) {
        ExerciseEntity e = exerciseRepository.findById(id).orElseThrow();
        return ExerciseResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .muscleGroup(e.getMuscleGroup())
                .equipment(e.getEquipment())
                .defaultTargetWeight(e.getDefaultTargetWeight())
                .defaultTargetReps(e.getDefaultTargetReps())
                .build();
    }
}

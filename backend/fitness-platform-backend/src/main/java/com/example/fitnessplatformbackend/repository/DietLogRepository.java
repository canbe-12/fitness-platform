package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.DietLogEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietLogRepository extends JpaRepository<DietLogEntity, Long> {

    Optional<DietLogEntity> findByUserIdAndClientRequestId(Long userId, String clientRequestId);

    List<DietLogEntity> findByUserIdAndDate(Long userId, LocalDate date);

    List<DietLogEntity> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate start, LocalDate end);

    Optional<DietLogEntity> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "items")
    Optional<DietLogEntity> findDetailByIdAndUserId(Long id, Long userId);
}

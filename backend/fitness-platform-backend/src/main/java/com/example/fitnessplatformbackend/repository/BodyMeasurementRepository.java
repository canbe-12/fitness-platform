package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.BodyMeasurementEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurementEntity, Long> {
    Optional<BodyMeasurementEntity> findByUserIdAndMeasureDate(Long userId, LocalDate measureDate);

    @Query("""
        select b from BodyMeasurementEntity b
        where b.userId = :uid and b.measureDate between :from and :to
        order by b.measureDate asc
    """)
    List<BodyMeasurementEntity> trend(@Param("uid") Long userId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

    @Query("""
        select b from BodyMeasurementEntity b
        where b.userId = :uid and b.measureDate = :d
    """)
    Optional<BodyMeasurementEntity> findOn(@Param("uid") Long userId, @Param("d") LocalDate date);
}

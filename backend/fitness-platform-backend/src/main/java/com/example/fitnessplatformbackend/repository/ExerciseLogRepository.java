package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.ExerciseLogEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

public interface ExerciseLogRepository extends JpaRepository<ExerciseLogEntity, Long> {
    List<ExerciseLogEntity> findByUserIdAndPlannedDateOrderByLoggedAtAsc(Long userId, LocalDate plannedDate);

    @Query("""
        select l from ExerciseLogEntity l
        where l.userId = :uid and l.plannedDate between :from and :to
        order by l.plannedDate asc, l.loggedAt asc
    """)
    List<ExerciseLogEntity> findRange(@Param("uid") Long userId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

    Optional<ExerciseLogEntity> findByUserIdAndClientRequestIdAndExerciseId(Long userId, String clientRequestId, Long exerciseId);

    @Query(value = """
      select date_format(planned_date, '%Y-%m-%d') as d,
             max(actual_weight) as maxWeight
      from exercise_log
      where user_id = :uid and exercise_id = :eid and planned_date between :from and :to
      group by planned_date
      order by planned_date
    """, nativeQuery = true)
    List<Object[]> trendMaxWeight(@Param("uid") Long userId,
                                  @Param("eid") Long exerciseId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    @Query(value = """
      select count(distinct planned_date)
      from exercise_log
      where user_id = :uid and planned_date between :from and :to
    """, nativeQuery = true)
    long countDistinctWorkoutDays(@Param("uid") Long userId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);
}

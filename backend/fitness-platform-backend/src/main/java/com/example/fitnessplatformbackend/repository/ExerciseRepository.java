package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.ExerciseEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    @Query("""
        select e from ExerciseEntity e
        where (:kw is null or :kw = '' or lower(e.name) like lower(concat('%', :kw, '%')))
          and (:mg is null or :mg = '' or e.muscleGroup = :mg)
    """)
    Page<ExerciseEntity> search(@Param("kw") String keyword,
                          @Param("mg") String muscleGroup,
                          Pageable pageable);
}

package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.ScheduledTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTaskEntity, Long> {
    List<ScheduledTaskEntity> findByUserIdAndTaskDateOrderByPriorityDesc(Long userId, LocalDate taskDate);
    
    // For calendar view (range query)
    List<ScheduledTaskEntity> findByUserIdAndTaskDateBetween(Long userId, LocalDate from, LocalDate to);
}

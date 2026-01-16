package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    List<FoodEntity> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
}

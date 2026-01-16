package com.example.fitnessplatformbackend.repository;

import com.example.fitnessplatformbackend.entity.UserPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPlanRepository extends JpaRepository<UserPlanEntity, Long> {
    Optional<UserPlanEntity> findByUserId(Long userId);
}

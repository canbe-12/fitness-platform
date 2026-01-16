package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.dto.plan.PlanResponse;
import com.example.fitnessplatformbackend.dto.plan.PlanUpsertRequest;
import com.example.fitnessplatformbackend.entity.UserPlanEntity;
import com.example.fitnessplatformbackend.repository.UserPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final UserPlanRepository userPlanRepository;

    public PlanResponse getCurrent(Long userId) {
        return userPlanRepository.findByUserId(userId)
                .map(this::toResp)
                .orElseGet(() -> new PlanResponse(
                        com.example.fitnessplatformbackend.entity.enums.PlanTarget.MAINTAIN,
                        3,
                        1800,
                        new java.math.BigDecimal("120"),
                        null,
                        null
                ));
    }

    @Transactional
    public PlanResponse upsertCurrent(Long userId, PlanUpsertRequest req) {
        LocalDateTime now = LocalDateTime.now();
        UserPlanEntity plan = userPlanRepository.findByUserId(userId).orElseGet(() ->
                UserPlanEntity.builder()
                        .userId(userId)
                        .createdAt(now)
                        .build()
        );

        plan.setTarget(req.getTarget());
        plan.setWeeklyWorkoutDays(req.getWeeklyWorkoutDays());
        plan.setDailyKcalTarget(req.getDailyKcalTarget());
        plan.setProteinTargetG(req.getProteinTargetG());
        plan.setUpdatedAt(now);

        return toResp(userPlanRepository.save(plan));
    }

    private PlanResponse toResp(UserPlanEntity p) {
        return new PlanResponse(
                p.getTarget(),
                p.getWeeklyWorkoutDays(),
                p.getDailyKcalTarget(),
                p.getProteinTargetG(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

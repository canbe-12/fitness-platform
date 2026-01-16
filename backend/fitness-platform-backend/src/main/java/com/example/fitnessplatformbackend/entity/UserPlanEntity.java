package com.example.fitnessplatformbackend.entity;

import com.example.fitnessplatformbackend.entity.enums.PlanTarget;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_plan", uniqueConstraints = @UniqueConstraint(name = "uk_plan_user", columnNames = "user_id"))
public class UserPlanEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanTarget target;

    @Column(name="weekly_workout_days", nullable = false)
    private Integer weeklyWorkoutDays;

    @Column(name="daily_kcal_target", nullable = false)
    private Integer dailyKcalTarget;

    @Column(name="protein_target_g", nullable = false, precision=10, scale=2)
    private BigDecimal proteinTargetG;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

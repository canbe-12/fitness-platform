package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="exercise_log",
        uniqueConstraints = @UniqueConstraint(
                name="uk_exlog_idempotent",
                columnNames={"user_id","client_request_id","exercise_id"}
        ))
@Getter @Setter @NoArgsConstructor
public class ExerciseLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="exercise_id", nullable=false)
    private Long exerciseId;

    @Column(name="workout_day_id")
    private Long workoutDayId;

    @Column(name="planned_date", nullable=false)
    private LocalDate plannedDate;

    @Column(name="logged_at", nullable=false)
    private LocalDateTime loggedAt;

    @Column(name="is_backfill", nullable=false)
    private Boolean isBackfill;

    @Column(name="planned_weight", precision=6, scale=2)
    private BigDecimal plannedWeight;

    @Column(name="planned_reps")
    private Integer plannedReps;

    @Column(name="actual_weight", nullable=false, precision=6, scale=2)
    private BigDecimal actualWeight;

    @Column(name="actual_reps", nullable=false)
    private Integer actualReps;

    @Column(name="client_request_id", nullable=false, length=64)
    private String clientRequestId;

    @Column(name="notes", length=255)
    private String notes;
}

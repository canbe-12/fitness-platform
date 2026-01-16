package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="exercise")
@Getter @Setter @NoArgsConstructor
public class ExerciseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=100)
    private String name;

    @Column(name="muscle_group", length=50)
    private String muscleGroup;

    @Column(name="equipment", length=50)
    private String equipment;

    @Column(name="default_target_weight", precision=6, scale=2)
    private BigDecimal defaultTargetWeight;

    @Column(name="default_target_reps")
    private Integer defaultTargetReps;

    @Column(name="met", precision=5, scale=2)
    private BigDecimal met;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

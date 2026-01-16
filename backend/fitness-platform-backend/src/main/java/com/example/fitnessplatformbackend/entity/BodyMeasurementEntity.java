package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="body_measurement",
        uniqueConstraints = @UniqueConstraint(
                name="uk_body_user_date",
                columnNames={"user_id","measure_date"}
        ))
@Getter @Setter @NoArgsConstructor
public class BodyMeasurementEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="measure_date", nullable=false)
    private LocalDate measureDate;

    @Column(name="weight_kg", nullable=false, precision=6, scale=2)
    private BigDecimal weightKg;

    @Column(name="body_fat_percent", precision=5, scale=2)
    private BigDecimal bodyFatPercent;

    @Column(name="waist_cm", precision=6, scale=2)
    private BigDecimal waistCm;

    @Column(name="notes", length=255)
    private String notes;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "diet_log_item", indexes = {
        @Index(name = "idx_dietitem_log", columnList = "diet_log_id")
})
public class DietLogItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="diet_log_id", nullable = false)
    private DietLogEntity dietLog;

    @Column(name="food_id")
    private Long foodId;

    @Column(name="food_name", nullable = false, length = 100)
    private String foodName;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, precision=10, scale=2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Integer kcal;

    @Column(name="protein_g", nullable = false, precision=10, scale=2)
    private BigDecimal proteinG;

    @Column(name="carb_g", nullable = false, precision=10, scale=2)
    private BigDecimal carbG;

    @Column(name="fat_g", nullable = false, precision=10, scale=2)
    private BigDecimal fatG;

    @Column(name="fiber_g", nullable = false, precision=10, scale=2)
    private BigDecimal fiberG;

    @Column(name="sodium_mg", nullable = false)
    private Integer sodiumMg;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

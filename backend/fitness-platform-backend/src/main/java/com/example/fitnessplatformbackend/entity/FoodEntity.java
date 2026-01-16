package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "food", indexes = {
        @Index(name = "idx_food_name", columnList = "name")
})
public class FoodEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Integer kcal;

    @Column(name = "protein_g", nullable = false, precision=10, scale=2)
    private java.math.BigDecimal proteinG;

    @Column(name = "carb_g", nullable = false, precision=10, scale=2)
    private java.math.BigDecimal carbG;

    @Column(name = "fat_g", nullable = false, precision=10, scale=2)
    private java.math.BigDecimal fatG;

    @Column(name = "fiber_g", nullable = false, precision=10, scale=2)
    private java.math.BigDecimal fiberG;

    @Column(name = "sodium_mg", nullable = false)
    private Integer sodiumMg;

    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

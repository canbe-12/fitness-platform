package com.example.fitnessplatformbackend.entity;

import com.example.fitnessplatformbackend.entity.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "diet_log",
        uniqueConstraints = @UniqueConstraint(name = "uk_dietlog_user_req", columnNames = {"user_id", "client_request_id"}),
        indexes = @Index(name = "idx_dietlog_user_date", columnList = "user_id,log_date")
)
public class DietLogEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="client_request_id", nullable = false, length = 64)
    private String clientRequestId;

    @Column(name="log_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name="meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(length = 255)
    private String note;

    // snapshot
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

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "dietLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DietLogItemEntity> items = new ArrayList<>();

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

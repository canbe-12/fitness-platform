package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.fitnessplatformbackend.entity.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "`user`")
@Getter @Setter @NoArgsConstructor
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", nullable=false, length=50)
    private String username;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Column(name="nickname")
    private String nickname;


    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;


    @Column(name="birthday")
    private LocalDate birthday;

    @Column(name="height_cm", precision=6, scale=2)
    private BigDecimal heightCm;

    @Column(name="weight_kg", precision=6, scale=2)
    private BigDecimal weightKg;

    @Column(name="goal")
    private String goal;

    @Column(name="level")
    private String level;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

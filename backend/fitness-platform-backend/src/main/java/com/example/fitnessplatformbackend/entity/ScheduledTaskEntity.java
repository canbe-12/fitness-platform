package com.example.fitnessplatformbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scheduled_task")
public class ScheduledTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, IN_PROGRESS, COMPLETED

    @Column(nullable = false)
    private Integer priority;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

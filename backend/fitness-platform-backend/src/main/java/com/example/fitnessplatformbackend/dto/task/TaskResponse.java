package com.example.fitnessplatformbackend.dto.task;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskResponse {
    private Long id;
    private LocalDate taskDate;
    private String title;
    private String description;
    private String status;
    private Integer priority;
}

package com.example.fitnessplatformbackend.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpsertRequest {
    @NotNull
    private LocalDate taskDate;

    @NotBlank
    private String title;

    private String description;
    
    private String status; // Optional for update
    private Integer priority;
}

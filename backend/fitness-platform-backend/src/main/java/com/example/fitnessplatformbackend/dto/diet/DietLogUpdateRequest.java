package com.example.fitnessplatformbackend.dto.diet;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DietLogUpdateRequest {

    @Size(max = 255)
    private String note;

}

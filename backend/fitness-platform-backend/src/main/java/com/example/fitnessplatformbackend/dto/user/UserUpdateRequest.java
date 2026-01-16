package com.example.fitnessplatformbackend.dto.user;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String gender;
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String goal;
    private String level;
}

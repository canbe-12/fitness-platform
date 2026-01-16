package com.example.fitnessplatformbackend.dto.user;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class UserMeResponse {
    private Long id;
    private String username;
    private String nickname;
    private String gender;
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String goal;
    private String level;
}

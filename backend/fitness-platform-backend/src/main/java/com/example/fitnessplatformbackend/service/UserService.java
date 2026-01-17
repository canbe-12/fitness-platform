package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.entity.BodyMeasurementEntity;
import com.example.fitnessplatformbackend.repository.BodyMeasurementRepository;
import com.example.fitnessplatformbackend.entity.UserEntity;
import com.example.fitnessplatformbackend.repository.UserRepository;
import com.example.fitnessplatformbackend.dto.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.fitnessplatformbackend.entity.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;

    private Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("gender 必须是 MALE/FEMALE/OTHER");
        }
    }

    private String formatGender(Gender gender) {
        return gender == null ? null : gender.name();
    }


    public UserMeResponse getMe(Long userId) {
        UserEntity u = userRepository.findById(userId).orElseThrow();
        return UserMeResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .gender(String.valueOf(u.getGender()))
                .birthday(u.getBirthday())
                .heightCm(u.getHeightCm())
                .weightKg(u.getWeightKg())
                .goal(u.getGoal())
                .level(u.getLevel())
                .build();
    }

    @Transactional
    public UserMeResponse updateMe(Long userId, UserUpdateRequest req) {
        UserEntity u = userRepository.findById(userId).orElseThrow();
        u.setNickname(req.getNickname());
        u.setGender(parseGender(req.getGender()));
        u.setBirthday(req.getBirthday());
        u.setHeightCm(req.getHeightCm());
        
        // Sync weight to BodyMeasurement if changed
        if (req.getWeightKg() != null) {
            u.setWeightKg(req.getWeightKg());
            // Create or update today's measurement
            LocalDate today = LocalDate.now();
            BodyMeasurementEntity bm = bodyMeasurementRepository.findByUserIdAndMeasureDate(userId, today)
                    .orElseGet(() -> {
                        BodyMeasurementEntity newBm = new BodyMeasurementEntity();
                        newBm.setUserId(userId);
                        newBm.setMeasureDate(today);
                        newBm.setCreatedAt(LocalDateTime.now());
                        return newBm;
                    });
            bm.setWeightKg(req.getWeightKg());
            bodyMeasurementRepository.save(bm);
        }
        
        u.setGoal(req.getGoal());
        u.setLevel(req.getLevel());
        return getMe(userId);
    }
}

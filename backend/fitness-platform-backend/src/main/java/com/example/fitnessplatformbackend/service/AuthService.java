package com.example.fitnessplatformbackend.service;

import com.example.fitnessplatformbackend.common.ApiException;
import com.example.fitnessplatformbackend.config.JwtTokenProvider;
import com.example.fitnessplatformbackend.entity.UserEntity;
import com.example.fitnessplatformbackend.entity.enums.Gender;
import com.example.fitnessplatformbackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final JwtTokenProvider jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepo, JwtTokenProvider jwt) {
        this.userRepo = userRepo;
        this.jwt = jwt;
    }

    public void register(String username, String password) {
        if (userRepo.existsByUsername(username)) {
            throw ApiException.conflict("用户名已存在");
        }
        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setGender(Gender.OTHER);
        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        userRepo.save(u);
    }

    public String login(String username, String password) {
        UserEntity u = userRepo.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("用户名或密码错误"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw ApiException.unauthorized("用户名或密码错误");
        }
        return jwt.createToken(u.getId(), u.getUsername());
    }

    public long expiresInSeconds() {
        return jwt.getExpireSeconds();
    }
}

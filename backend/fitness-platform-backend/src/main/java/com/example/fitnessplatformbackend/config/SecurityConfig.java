package com.example.fitnessplatformbackend.config;

import com.example.fitnessplatformbackend.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expireSeconds}") long expireSeconds) {
        return new JwtTokenProvider(secret, expireSeconds);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider provider, ObjectMapper om) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/**",
                        "/error",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()

                .anyRequest().authenticated()
        );

        http.addFilterBefore(new JwtAuthenticationFilter(provider), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            om.writeValue(res.getWriter(), ApiResponse.fail(40101, "UNAUTHORIZED"));
        }));

        return http.build();
    }
}

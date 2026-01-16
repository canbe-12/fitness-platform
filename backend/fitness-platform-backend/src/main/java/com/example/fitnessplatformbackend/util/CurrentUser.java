package com.example.fitnessplatformbackend.util;

import com.example.fitnessplatformbackend.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {
    public static Long id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) throw ApiException.unauthorized("UNAUTHORIZED");
        return (Long) a.getPrincipal();
    }
}

package com.smarthospital.mapper;

import com.smarthospital.dto.auth.AuthResponse;
import com.smarthospital.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public AuthResponse toAuthResponse(User user, String token, long expiresAt) {
        return new AuthResponse(token, user.getRole(), user.getName(), user.getId(), expiresAt);
    }
}

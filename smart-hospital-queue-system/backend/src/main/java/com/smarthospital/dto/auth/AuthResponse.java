package com.smarthospital.dto.auth;

import com.smarthospital.entity.Role;

public class AuthResponse {
    private String token;
    private Role role;
    private String name;
    private String userId;
    private long expiresAt;

    public AuthResponse(String token, Role role, String name, String userId, long expiresAt) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public String getUserId() { return userId; }
    public long getExpiresAt() { return expiresAt; }
}

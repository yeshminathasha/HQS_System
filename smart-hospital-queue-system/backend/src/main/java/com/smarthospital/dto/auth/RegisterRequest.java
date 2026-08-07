package com.smarthospital.dto.auth;

import com.smarthospital.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @Size(min = 6)
    private String password;

    // Only honored when the caller is an authenticated ADMIN hitting
    // /api/users. Public /api/auth/register always forces role=USER
    // in the service layer regardless of what's sent here.
    private Role role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

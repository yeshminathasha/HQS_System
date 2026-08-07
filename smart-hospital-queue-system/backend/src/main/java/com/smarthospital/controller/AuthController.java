package com.smarthospital.controller;

import com.smarthospital.dto.auth.*;
import com.smarthospital.entity.User;
import com.smarthospital.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public User me(Authentication authentication) {
        // authentication.getName() == the email set as the JWT subject
        return authService.getCurrentUser(authentication.getName());
    }
}

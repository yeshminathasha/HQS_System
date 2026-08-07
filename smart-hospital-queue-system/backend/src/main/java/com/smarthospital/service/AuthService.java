package com.smarthospital.service;

import com.smarthospital.dto.auth.*;
import com.smarthospital.entity.Role;
import com.smarthospital.entity.User;
import com.smarthospital.exception.DuplicateEmailException;
import com.smarthospital.exception.InvalidCredentialsException;
import com.smarthospital.mapper.UserMapper;
import com.smarthospital.repository.UserRepository;
import com.smarthospital.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // Public self-registration is ALWAYS role=USER, regardless of what
        // the client sends. Admin-created accounts go through UserService instead.
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        long expiresAt = System.currentTimeMillis() + jwtUtil.getExpirationMs();
        return userMapper.toAuthResponse(user, token, expiresAt);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user);
        long expiresAt = System.currentTimeMillis() + jwtUtil.getExpirationMs();
        return userMapper.toAuthResponse(user, token, expiresAt);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }
}

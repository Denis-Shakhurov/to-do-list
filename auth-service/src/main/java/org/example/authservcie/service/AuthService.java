package org.example.authservcie.service;

import lombok.RequiredArgsConstructor;
import org.example.authservcie.dto.AuthRequest;
import org.example.authservcie.dto.AuthResponse;
import org.example.authservcie.dto.RegisterRequest;
import org.example.authservcie.exception.AlreadyExistsException;
import org.example.authservcie.exception.ResourceNotFoundException;
import org.example.authservcie.model.Role;
import org.example.authservcie.model.User;
import org.example.authservcie.repository.UserRepository;
import org.example.authservcie.security.JwtService;
import org.example.authservcie.security.UserDetailsImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(new UserDetailsImpl(user));
        String refreshToken = jwtService.generateRefreshToken(new UserDetailsImpl(user));
        refreshTokenService.saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String accessToken = null;
        String refreshToken = null;

        if (user.getPassword().equals(request.getPassword())) {
            accessToken = jwtService.generateAccessToken(new UserDetailsImpl(user));
            refreshToken = jwtService.generateRefreshToken(new UserDetailsImpl(user));
            refreshTokenService.saveRefreshToken(user, refreshToken);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}

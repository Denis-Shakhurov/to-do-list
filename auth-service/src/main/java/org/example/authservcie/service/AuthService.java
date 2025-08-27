package org.example.authservcie.service;

import lombok.RequiredArgsConstructor;
import org.example.authservcie.dto.AuthRequest;
import org.example.authservcie.dto.AuthResponse;
import org.example.authservcie.dto.CreateUserProfileRequest;
import org.example.authservcie.dto.RegisterRequest;
import org.example.authservcie.exception.AlreadyExistsException;
import org.example.authservcie.exception.ResourceNotFoundException;
import org.example.authservcie.model.User;
import org.example.authservcie.repository.UserRepository;
import org.example.authservcie.security.JwtService;
import org.example.authservcie.service.client.UserProfileClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserProfileClient userProfileClient;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        // Сохраняем пользователя сразу
        user = userRepository.save(user);
        Long userId = user.getId();

        try {
            // Пытаемся создать профиль в user-service
            userProfileClient.createUser(new CreateUserProfileRequest(
                    userId,
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            ));
        } catch (Exception e) {
            // Если не удалось - удаляем пользователя (компенсация)
            userRepository.deleteById(userId);
            throw new RuntimeException("Failed to create user profile", e);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .id(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String accessToken = null;
        String refreshToken = null;

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            accessToken = jwtService.generateAccessToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            refreshTokenService.saveRefreshToken(user, refreshToken);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean validateToken(String token) {
        String username = jwtService.extractUsername(token);

        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();

        return jwtService.validateToken(token, userDetails);
    }
}

package org.example.authservcie.service;

import lombok.RequiredArgsConstructor;
import org.example.authservcie.config.JwtConfig;
import org.example.authservcie.model.RefreshToken;
import org.example.authservcie.model.User;
import org.example.authservcie.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    public void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusMillis(jwtConfig.refreshExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}

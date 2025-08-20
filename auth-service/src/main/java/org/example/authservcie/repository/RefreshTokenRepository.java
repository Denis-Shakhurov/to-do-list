package org.example.authservcie.repository;

import org.example.authservcie.model.RefreshToken;
import org.example.authservcie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findRefreshTokenByToken(String token);
    void deleteByUser(User user);
}
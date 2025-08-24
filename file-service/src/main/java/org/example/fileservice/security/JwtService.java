package org.example.fileservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    public boolean validationToken(String token, UserDetails userDetails) {
        try {
            String userName = extractUserName(token);
            return userName.equals(userDetails.getUsername()) && !isExpiredToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserName(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isExpiredToken(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}

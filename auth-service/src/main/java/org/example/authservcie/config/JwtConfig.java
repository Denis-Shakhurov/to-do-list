package org.example.authservcie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Bean
    public String jwtSecret(){
        return secret;
    }

    @Bean
    public Long refreshExpiration() {
        return refreshExpiration;
    }

    @Bean
    public Long accessExpiration() {
        return accessExpiration;
    }
}

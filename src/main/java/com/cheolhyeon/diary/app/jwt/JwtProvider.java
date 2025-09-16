package com.cheolhyeon.diary.app.jwt;

import com.cheolhyeon.diary.app.properties.JwtProperties;
import com.cheolhyeon.diary.token.dto.request.JwtRequest;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtRequest createJwt(Long kakaoId) {
        Date now = new Date();
        String accessToken = generateAccessToken(kakaoId);
        String refreshToken = generateRefreshToken(kakaoId);
        long refresh = getExpirationDate(now, "REFRESH");
        return JwtRequest.builder()
                .userId(kakaoId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createDate(now)
                .refreshExpireDate(refresh)
                .build();
    }

    private String generateAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(getExpirationDate(now, "ACCESS"));
        return Jwts.builder()
                .subject("Access Token")
                .claim("userId", userId)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(getExpirationDate(now, "REFRESH"));
        return Jwts.builder()
                .subject("Refresh Token")
                .claim("userId", userId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private long getExpirationDate(Date now, String tokenType) {
        if (tokenType.equals("REFRESH")) {
            return now.getTime() + jwtProperties.getRefreshTokenExpiration();
        }
        return now.getTime() + jwtProperties.getAccessTokenExpiration();
    }

    public Long getUserIdFromToken(String token) {
        return (Long) Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId");
    }

    public String getTokenType(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}

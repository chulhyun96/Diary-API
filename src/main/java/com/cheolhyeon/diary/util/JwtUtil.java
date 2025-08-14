package com.cheolhyeon.diary.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String kakaoId, String nickname) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        log.info("🔐 JWT 토큰 생성 시작 - kakaoId: {}, nickname: {}", kakaoId, nickname);
        log.info("📅 토큰 만료 시간: {} ({}분 후)", expiryDate, expiration / 60000);

        String token = Jwts.builder()
                .subject(kakaoId)
                .claim("nickname", nickname)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        log.info("✅ JWT 토큰 생성 완료 - 토큰 길이: {} 문자", token.length());
        log.debug("🔍 생성된 JWT 토큰: {}", token);

        return token;
    }

    public String getKakaoIdFromToken(String token) {
        log.debug("🔍 JWT 토큰에서 kakaoId 추출 시작");
        
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String kakaoId = claims.getSubject();
        log.info("✅ JWT 토큰에서 kakaoId 추출 완료: {}", kakaoId);
        
        return kakaoId;
    }

    public String getNicknameFromToken(String token) {
        log.debug("🔍 JWT 토큰에서 nickname 추출 시작");
        
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String nickname = claims.get("nickname", String.class);
        log.info("✅ JWT 토큰에서 nickname 추출 완료: {}", nickname);
        
        return nickname;
    }

    public boolean validateToken(String token) {
        log.debug("🔍 JWT 토큰 검증 시작");
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            boolean isValid = !expiration.before(now);
            
            if (isValid) {
                log.info("✅ JWT 토큰 검증 성공 - 만료 시간: {}", expiration);
            } else {
                log.warn("⚠️ JWT 토큰 만료됨 - 만료 시간: {}, 현재 시간: {}", expiration, now);
            }
            
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        log.debug("🔍 JWT 토큰 만료 여부 확인 시작");
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();
            boolean isExpired = expiration.before(now);
            
            log.info("📅 JWT 토큰 만료 여부: {} (만료 시간: {}, 현재 시간: {})", 
                    isExpired ? "만료됨" : "유효함", expiration, now);
            
            return isExpired;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ JWT 토큰 만료 확인 중 오류: {}", e.getMessage());
            return true;
        }
    }
} 
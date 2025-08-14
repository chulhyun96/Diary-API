package com.cheolhyeon.diary.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-generation-and-validation");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24시간
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Given
        String kakaoId = "123456789";
        String nickname = "테스트유저";

        // When
        String token = jwtUtil.generateToken(kakaoId, nickname);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void getKakaoIdFromToken_ShouldExtractCorrectKakaoId() {
        // Given
        String kakaoId = "123456789";
        String nickname = "테스트유저";
        String token = jwtUtil.generateToken(kakaoId, nickname);

        // When
        String extractedKakaoId = jwtUtil.getKakaoIdFromToken(token);

        // Then
        assertEquals(kakaoId, extractedKakaoId);
    }

    @Test
    void getNicknameFromToken_ShouldExtractCorrectNickname() {
        // Given
        String kakaoId = "123456789";
        String nickname = "테스트유저";
        String token = jwtUtil.generateToken(kakaoId, nickname);

        // When
        String extractedNickname = jwtUtil.getNicknameFromToken(token);

        // Then
        assertEquals(nickname, extractedNickname);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken("123456789", "테스트유저");

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken("");

        // Then
        assertFalse(isValid);
    }
} 
package com.cheolhyeon.diary.auth.jwt;

import com.cheolhyeon.diary.app.properties.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    JwtProperties jwtProperties;

    @InjectMocks
    JwtProvider jwtProvider;

    private final String testSecret = "testSecretKeyForJwtTokenGenerationAndValidationTest";
    private final String testIssuer = "test-issuer";
    private final String testRtHmacSecret = Base64.getEncoder().encodeToString("testRtHmacSecret".getBytes());
    private final int accessTokenExpiration = 3600000; // 1시간
    private final int rtLengthBytes = 32;

    @Test
    @DisplayName("Access Token 생성 성공 테스트")
    void generateAccessToken_Success() {
        // Given
        given(jwtProperties.getSecret()).willReturn(testSecret);
        given(jwtProperties.getIss()).willReturn(testIssuer);
        given(jwtProperties.getAccessTokenExpiration()).willReturn(accessTokenExpiration);
        
        Long userId = 1L;
        String sessionId = "test-session-id";

        // When
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId);

        // Then
        assertThat(accessToken).isNotNull()
                .isNotEmpty();
        assertThat(accessToken.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
    }

    @Test
    @DisplayName("Access Token에서 Session ID 추출 성공 테스트")
    void getSessionIdFromAccessToken_Success() {
        // Given
        given(jwtProperties.getSecret()).willReturn(testSecret);
        given(jwtProperties.getIss()).willReturn(testIssuer);
        given(jwtProperties.getAccessTokenExpiration()).willReturn(accessTokenExpiration);
        
        Long userId = 1L;
        String sessionId = "test-session-id";
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId);

        // When
        String extractedSessionId = jwtProvider.getSessionIdFromAccessToken(accessToken);

        // Then
        assertThat(extractedSessionId).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Access Token에서 User ID 추출 성공 테스트")
    void getUserIdFromAccessToken_Success() {
        // Given
        given(jwtProperties.getSecret()).willReturn(testSecret);
        given(jwtProperties.getIss()).willReturn(testIssuer);
        given(jwtProperties.getAccessTokenExpiration()).willReturn(accessTokenExpiration);
        
        Long userId = 1L;
        String sessionId = "test-session-id";
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId);

        // When
        Long extractedUserId = jwtProvider.getUserIdFromAccessToken(accessToken);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Access Token 유효성 검증 성공 테스트")
    void validateAccessToken_ValidToken_Success() {
        // Given
        given(jwtProperties.getSecret()).willReturn(testSecret);
        given(jwtProperties.getIss()).willReturn(testIssuer);
        given(jwtProperties.getAccessTokenExpiration()).willReturn(accessTokenExpiration);
        
        Long userId = 1L;
        String sessionId = "test-session-id";
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId);

        // When
        boolean isValid = jwtProvider.validateAccessToken(accessToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Access Token 유효성 검증 실패 - 잘못된 서명")
    void validateAccessToken_InvalidSignature_Failure() {
        // Given
        given(jwtProperties.getSecret()).willReturn(testSecret);
        
        String invalidToken = "invalid.token.signature";

        // When
        boolean isValid = jwtProvider.validateAccessToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Opaque Refresh Token 생성 성공 테스트")
    void generateOpaqueRT_Success() {
        // Given
        given(jwtProperties.getRtLengthBytes()).willReturn(rtLengthBytes);
        
        // When
        String refreshToken = jwtProvider.generateOpaqueRT();

        // Then
        assertThat(refreshToken)
                .isNotNull()
                .isNotEmpty()
                .matches("^[A-Za-z0-9_-]+$");
    }

    @Test
    @DisplayName("Refresh Token 해시 생성 성공 테스트")
    void hashRT_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        // Given
        given(jwtProperties.getRtHmacSecret()).willReturn(testRtHmacSecret);
        given(jwtProperties.getRtLengthBytes()).willReturn(rtLengthBytes);

        String refreshToken = jwtProvider.generateOpaqueRT();

        // When
        String hashedToken = jwtProvider.hashRT(refreshToken);

        // Then
        assertThat(hashedToken).isNotNull()
                .isNotEmpty()
                .isNotEqualTo(refreshToken)
                .matches("^[A-Za-z0-9_-]+$");
    }

    @Test
    @DisplayName("Access Token 만료 시간 계산 테스트")
    void getExpirationDate_AccessToken_Success() {
        // Given
        given(jwtProperties.getAccessTokenExpiration()).willReturn(accessTokenExpiration);
        
        long currentTime = System.currentTimeMillis();
        Date now = new Date(currentTime);

        // When
        long expirationTime = jwtProvider.getExpirationDate(now, "ACCESS");

        // Then
        assertThat(expirationTime).isEqualTo(currentTime + accessTokenExpiration);
    }

    @Test
    @DisplayName("Refresh Token 만료 시간 계산 테스트")
    void getExpirationDate_RefreshToken_Success() {
        // Given
        // 7일
        int refreshTokenExpiration = 604800000;
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(refreshTokenExpiration);
        
        long currentTime = System.currentTimeMillis();
        Date now = new Date(currentTime);

        // When
        long expirationTime = jwtProvider.getExpirationDate(now, "REFRESH");

        // Then
        assertThat(expirationTime).isEqualTo(currentTime + refreshTokenExpiration);
    }
}
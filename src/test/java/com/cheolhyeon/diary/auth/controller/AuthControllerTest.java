package com.cheolhyeon.diary.auth.controller;

import com.cheolhyeon.diary.auth.service.AuthService;
import com.cheolhyeon.diary.auth.session.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    AuthService authService;

    @Mock
    SessionService sessionService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    AuthController authController;

    @Test
    @DisplayName("로그인 URL 조회 성공")
    void login_Success() {
        // Given
        String expectedUrl = "https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=test";
        given(authService.getLoginUrl()).willReturn(expectedUrl);

        // When
        ResponseEntity<?> result = authController.login();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(java.util.Map.class);
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> body = (java.util.Map<String, String>) result.getBody();
        assertThat(Objects.requireNonNull(body).get("loginUrl")).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 유효한 쿠키")
    void refresh_ValidCookies_Success() {
        // Given
        Cookie[] cookies = {
            createCookie("__HOST-SID", "test-session-id"),
            createCookie("__HOST-RT", "test-refresh-token")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessionService).refreshSession(eq("test-session-id"), eq("test-refresh-token"), any(LocalDateTime.class), eq(response));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 쿠키가 없는 경우")
    void refresh_NoCookies_Unauthorized() {
        // Given
        given(request.getCookies()).willReturn(null);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - SID 쿠키가 없는 경우")
    void refresh_NoSidCookie_Unauthorized() {
        // Given
        Cookie[] cookies = {
            createCookie("__HOST-RT", "test-refresh-token")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - RT 쿠키가 없는 경우")
    void refresh_NoRtCookie_Unauthorized() {
        // Given
        Cookie[] cookies = {
            createCookie("__HOST-SID", "test-session-id")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 빈 쿠키 값")
    void refresh_EmptyCookieValues_Unauthorized() {
        // Given
        Cookie[] cookies = {
            createCookie("__HOST-SID", ""),
            createCookie("__HOST-RT", "")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - null 쿠키 값")
    void refresh_NullCookieValues_Unauthorized() {
        // Given
        Cookie[] cookies = {
            createCookie("__HOST-SID", null),
            createCookie("__HOST-RT", null)
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 다른 쿠키들만 있는 경우")
    void refresh_OtherCookiesOnly_Unauthorized() {
        // Given
        Cookie[] cookies = {
            createCookie("other-cookie", "value1"),
            createCookie("another-cookie", "value2")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sessionService, never()).refreshSession(anyString(), anyString(), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 여러 쿠키 중에서 필요한 쿠키만 추출")
    void refresh_MultipleCookies_ExtractRequiredCookies_Success() {
        // Given
        Cookie[] cookies = {
            createCookie("other-cookie", "value1"),
            createCookie("__HOST-SID", "test-session-id"),
            createCookie("another-cookie", "value2"),
            createCookie("__HOST-RT", "test-refresh-token"),
            createCookie("last-cookie", "value3")
        };
        given(request.getCookies()).willReturn(cookies);

        // When
        ResponseEntity<Void> result = authController.refresh(response, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(sessionService).refreshSession(eq("test-session-id"), eq("test-refresh-token"), any(LocalDateTime.class), eq(response));
    }

    private Cookie createCookie(String name, String value) {
        return new Cookie(name, value == null ? "" : value);
    }
}
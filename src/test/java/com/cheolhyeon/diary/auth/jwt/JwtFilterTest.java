package com.cheolhyeon.diary.auth.jwt;

import com.cheolhyeon.diary.auth.entity.AuthSession;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.auth.session.SessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    JwtProvider jwtProvider;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Mock
    PrintWriter printWriter;

    @InjectMocks
    JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증이 필요 없는 경로 - shouldNotFilter true 반환")
    void shouldNotFilter_PublicPaths_ReturnsTrue()  {
        // Given
        String[] publicPaths = {
            "/",
            "/index.html",
            "/login",
            "/auth/login",
            "/auth/register",
            "/.well-known/",
            "/style.css",
            "/script.js",
            "/favicon.ico",
            "/js/app.js",
            "/css/style.css",
            "/assets/image.png"
        };

        for (String path : publicPaths) {
            // When
            given(request.getRequestURI()).willReturn(path);
            boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isTrue();
        }
    }

    @Test
    @DisplayName("인증이 필요한 경로 - shouldNotFilter false 반환")
    void shouldNotFilter_ProtectedPaths_ReturnsFalse()  {
        // Given
        String[] protectedPaths = {
            "/api/diary",
            "/api/user/profile",
            "/api/diary/123",
            "/admin/dashboard"
        };

        for (String path : protectedPaths) {
            // When
            given(request.getRequestURI()).willReturn(path);
            boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isFalse();
        }
    }

    @Test
    @DisplayName("Bearer 토큰이 있는 경우 - SecurityContext 설정 성공")
    void doFilterInternal_WithBearerToken_SetsSecurityContext() throws ServletException, IOException {
        // Given
        String bearerToken = "Bearer valid.jwt.token";
        String accessToken = "valid.jwt.token";
        Long userId = 1L;
        String sessionId = "test-session-id";
        
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(true);
        given(jwtProvider.getUserIdFromAccessToken(accessToken)).willReturn(userId);
        given(jwtProvider.getSessionIdFromAccessToken(accessToken)).willReturn(sessionId);
        given(sessionRepository.findById(sessionId)).willReturn(Optional.of(mock(AuthSession.class)));

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되었는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isInstanceOf(CustomUserPrincipal.class);
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Bearer 토큰이 없는 경우 - SecurityContext 설정 안됨")
    void doFilterInternal_WithoutBearerToken_DoesNotSetSecurityContext() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 있지만 Bearer가 아닌 경우 - SecurityContext 설정 안됨")
    void doFilterInternal_WithNonBearerToken_DoesNotSetSecurityContext() throws ServletException, IOException {
        // Given
        String nonBearerToken = "Basic dXNlcjpwYXNzd29yZA=="; // Basic 인증
        given(request.getHeader("Authorization")).willReturn(nonBearerToken);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("빈 Authorization 헤더 - SecurityContext 설정 안됨")
    void doFilterInternal_WithEmptyAuthorizationHeader_DoesNotSetSecurityContext() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn("");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer 토큰이지만 공백만 있는 경우 - SecurityContext 설정 안됨")
    void doFilterInternal_WithBearerSpaceOnly_DoesNotSetSecurityContext() throws ServletException, IOException {
        // Given
        String bearerSpaceOnly = "Bearer ";
        given(request.getHeader("Authorization")).willReturn(bearerSpaceOnly);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 토큰 - SecurityContext 설정 안됨")
    void doFilterInternal_WithInvalidToken_DoesNotSetSecurityContext() throws ServletException, IOException {
        // Given
        String bearerToken = "Bearer invalid.jwt.token";
        String accessToken = "invalid.jwt.token";
        
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Single Session 정책 위반 - 401 상태코드와 -1 에러코드 반환")
    void doFilterInternal_SingleSessionPolicyViolation_Returns401WithErrorCodeMinus1() throws ServletException, IOException {
        // Given
        String bearerToken = "Bearer valid.jwt.token";
        String accessToken = "valid.jwt.token";
        Long userId = 1L;
        String sessionId = "test-session-id";
        
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtProvider.validateAccessToken(accessToken)).willReturn(true);
        given(jwtProvider.getUserIdFromAccessToken(accessToken)).willReturn(userId);
        given(jwtProvider.getSessionIdFromAccessToken(accessToken)).willReturn(sessionId);
        given(sessionRepository.findById(sessionId)).willReturn(Optional.empty()); // 세션을 찾을 수 없음
        given(response.getWriter()).willReturn(printWriter);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(request, response); // filterChain이 호출되지 않아야 함
        verify(response).setStatus(-1); // ONLY_SINGLE_SESSION.getErrorCode() = -1
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response.getWriter()).write(anyString());
        verify(response.getWriter()).flush();
        
        // SecurityContext가 설정되지 않았는지 확인
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
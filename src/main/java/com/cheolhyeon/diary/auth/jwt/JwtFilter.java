package com.cheolhyeon.diary.auth.jwt;

import com.cheolhyeon.diary.app.exception.dto.ErrorResponse;
import com.cheolhyeon.diary.app.exception.session.SessionErrorStatus;
import com.cheolhyeon.diary.app.exception.session.SessionException;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.auth.token.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final SessionRepository sessionRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        return requestURI.equals("/") ||
                requestURI.equals("/index.html") ||
                requestURI.startsWith("/login") ||
                requestURI.startsWith("/auth/") ||
                requestURI.startsWith("/.well-known/") ||  // 브라우저 특수 요청
                requestURI.endsWith(".html") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".ico") ||
                requestURI.endsWith(".map") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/assets/") ||
                requestURI.startsWith("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 더 앞선 필터에서 SecurityContextHolder에서 채울 수도 있음.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 "Bearer " 접두사 제거
        String accessToken = extractBearer(request);
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        //  토큰 유효성 검증 메서드 구현
        if (!jwtProvider.validateAccessToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        //  사용자 인증 정보 설정 메서드 구현
        Long userId = jwtProvider.getUserIdFromAccessToken(accessToken);
        String sessionId = jwtProvider.getSessionIdFromAccessToken(accessToken);

        try {
            sessionRepository.findById(sessionId).
                    orElseThrow(() -> new SessionException(SessionErrorStatus.ONLY_SINGLE_SESSION));
        } catch (SessionException e) {
            log.info("Single Session Policy: {} - USER ID: {}, SESSION ID: {}",
                    e.getMessage(), userId, sessionId);
            
            response.setStatus(SessionErrorStatus.ONLY_SINGLE_SESSION.getErrorCode());
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            
            ErrorResponse errorResponse = ErrorResponse.of(e.getErrorStatus());
            String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);
            
            log.debug("세션 정책 위반 응답 전송: {}", jsonResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            return;
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(userId, sessionId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private String extractBearer(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        authorizationHeader = authorizationHeader.trim();
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorizationHeader.substring(7);
        return token.isEmpty() ? null : token;
    }
}

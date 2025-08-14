package com.cheolhyeon.diary.security;

import com.cheolhyeon.diary.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("🔍 JWT 필터 처리 시작 - URI: {}", requestURI);

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                log.debug("🔑 Authorization 헤더에서 토큰 발견");

                if (jwtUtil.validateToken(token)) {
                    String kakaoId = jwtUtil.getKakaoIdFromToken(token);
                    String nickname = jwtUtil.getNicknameFromToken(token);

                    log.info("✅ JWT 토큰 검증 성공 - 사용자: {} ({})", nickname, kakaoId);

                    // 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(kakaoId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("🔐 SecurityContext에 인증 정보 설정 완료 - 사용자: {}", kakaoId);
                } else {
                    log.warn("⚠️ JWT 토큰 검증 실패 - URI: {}", requestURI);
                }
            } else {
                log.debug("🔍 Authorization 헤더에 토큰 없음 - URI: {}", requestURI);
            }
        } catch (Exception e) {
            log.error("❌ JWT 필터 처리 중 오류 발생 - URI: {}, 오류: {}", requestURI, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("🔑 Bearer 토큰 추출 완료 - 길이: {} 문자", token.length());
            return token;
        }

        return null;
    }
} 
package com.cheolhyeon.diary.security;

import com.cheolhyeon.diary.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        log.info("🎉 OAuth2 로그인 성공 핸들러 시작");
        log.info("📍 요청 URL: {}", request.getRequestURL());
        log.info("🔍 인증 객체 타입: {}", authentication.getPrincipal().getClass().getSimpleName());
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        if (oauth2User instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) oauth2User;
            
            log.info("👤 카카오 사용자 정보:");
            log.info("   - 카카오 ID: {}", customOAuth2User.getKakaoId());
            log.info("   - 닉네임: {}", customOAuth2User.getNickname());

            // JWT 토큰 생성
            log.info("🔐 JWT 토큰 생성 시작");
            String token = jwtUtil.generateToken(
                customOAuth2User.getKakaoId(),
                customOAuth2User.getNickname()
            );
            log.info("✅ JWT 토큰 생성 완료");
            
            // JSON 응답으로 토큰 전달 (리다이렉트 대신)
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            
            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "로그인 성공");
            responseData.put("token", token);
            responseData.put("user", Map.of(
                "kakaoId", customOAuth2User.getKakaoId(),
                "nickname", customOAuth2User.getNickname()
            ));
            
            log.info("📤 JSON 응답 전송");
            objectMapper.writeValue(response.getWriter(), responseData);
            log.info("✅ OAuth2 로그인 성공 처리 완료");
            
        } else {
            // 예상치 못한 OAuth2User 타입
            log.error("❌ 예상치 못한 OAuth2User 타입: {}", oauth2User.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "인증 처리 중 오류가 발생했습니다.");
            objectMapper.writeValue(response.getWriter(), errorData);
        }
    }
} 
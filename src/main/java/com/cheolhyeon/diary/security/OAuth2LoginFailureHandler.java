package com.cheolhyeon.diary.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        log.error("❌ OAuth2 로그인 실패 핸들러 시작");
        log.error("📍 요청 URL: {}", request.getRequestURL());
        log.error("🔍 예외 타입: {}", exception.getClass().getSimpleName());
        log.error("📋 예외 메시지: {}", exception.getMessage());
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            log.error("🔍 OAuth2 오류 코드: {}", oauth2Exception.getError().getErrorCode());
            log.error("📋 OAuth2 오류 설명: {}", oauth2Exception.getError().getDescription());
        }
        
        // 스택 트레이스 출력
        log.error("📋 상세 오류 정보:", exception);
        
        // 응답 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 오류 응답 데이터 생성
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("success", false);
        errorData.put("message", "OAuth2 로그인 실패");
        errorData.put("error", exception.getMessage());
        errorData.put("errorType", exception.getClass().getSimpleName());
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            errorData.put("oauth2ErrorCode", oauth2Exception.getError().getErrorCode());
            errorData.put("oauth2ErrorDescription", oauth2Exception.getError().getDescription());
        }
        
        // JSON 응답 전송
        objectMapper.writeValue(response.getWriter(), errorData);
        log.error("✅ OAuth2 로그인 실패 처리 완료");
    }
} 
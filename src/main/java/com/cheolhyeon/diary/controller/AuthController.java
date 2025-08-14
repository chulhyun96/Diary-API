package com.cheolhyeon.diary.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인증된 사용자입니다.");
            response.put("user", Map.of(
                "kakaoId", authentication.getName(),
                "authenticated", true
            ));
            
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "인증되지 않은 사용자입니다.");
        
        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> loginInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "카카오 로그인을 진행하세요.");
        response.put("loginUrl", "/oauth2/authorization/kakao");
        
        return ResponseEntity.ok(response);
    }
} 
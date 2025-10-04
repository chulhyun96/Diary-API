
package com.cheolhyeon.diary.auth.controller;

import com.cheolhyeon.diary.app.util.UlidGenerator;
import com.cheolhyeon.diary.auth.service.AuthService;
import com.cheolhyeon.diary.auth.token.SessionService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;

    @GetMapping("/login/oauth2/code/kakao")
    public void setAccessTokenByRedirect(String code, HttpServletResponse response, HttpServletRequest request) throws IOException {
        Long userId = authService.processLogin(code);
        String sessionId = UlidGenerator.generatorUlidForSession();
        String accessToken = sessionService.setAccessTokenToHeader(userId, sessionId, response);
        sessionService.setRefreshTokenToCookie(userId, sessionId, response, request);
        response.sendRedirect("/?login=success&token=" + accessToken);
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        String url = authService.getLoginUrl();
        return ResponseEntity
                .ok(Map.of("loginUrl", url));
    }
    @PostMapping("/auth/refresh")
    public ResponseEntity<Void> refresh(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, String> cookieMap = Arrays.stream(cookies)
                .filter(cookie -> "__HOST-SID".equals(cookie.getName())
                        || "__HOST-RT".equals(cookie.getName()))
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
        String sid = cookieMap.get("__HOST-SID");
        String rtPlain = cookieMap.get("__HOST-RT");
        if (StringUtils.isEmpty(sid) || StringUtils.isEmpty(rtPlain)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        sessionService.refreshSession(sid, rtPlain, LocalDateTime.now(), response);
        return ResponseEntity.ok().build();
    }
}

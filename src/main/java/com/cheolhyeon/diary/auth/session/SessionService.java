package com.cheolhyeon.diary.auth.session;

import com.cheolhyeon.diary.app.event.session.SessionInvalidatedEvent;
import com.cheolhyeon.diary.app.exception.session.SessionErrorStatus;
import com.cheolhyeon.diary.app.exception.session.SessionException;
import com.cheolhyeon.diary.auth.entity.AuthSession;
import com.cheolhyeon.diary.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SessionService {
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionRepository sessionRepository;

    public String setAccessTokenToHeader(Long userId, String sessionId, HttpServletResponse response) {
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId);
        response.setHeader("Authorization", "Bearer " + accessToken);
        return accessToken;
    }

    @Transactional
    public void setRefreshTokenToCookie(Long userId, String sessionId, HttpServletResponse response, HttpServletRequest request) {
        Optional<AuthSession> oldSession = sessionRepository.findByUserId(userId);
        oldSession.ifPresent(session -> {
            sessionRepository.delete(session);
            // SSE 정리 이벤트 발행
            eventPublisher.publishEvent(new SessionInvalidatedEvent(session.getSessionId())
            );
        });

        String clientIp = getClientRealIp(request);
        newSession(userId, sessionId, response, request, clientIp);
    }

    private void newSession(Long userId, String sessionId, HttpServletResponse response, HttpServletRequest request, String clientIp) {
        String refreshTokenPlain = jwtProvider.generateOpaqueRT();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastRefreshAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusDays(5);
        String ua = request.getHeader("User-Agent");

        String currentHashRT = jwtProvider.hashRT(refreshTokenPlain);

        AuthSession authSession =
                new AuthSession(
                        sessionId, userId, currentHashRT,
                        null, createdAt, lastRefreshAt,
                        expiresAt, ua, clientIp);
        sessionRepository.save(authSession);
        setCookie(response, refreshTokenPlain, sessionId);
    }

    @Transactional
    public void refreshSession(String sid, String rtPlain, LocalDateTime now, HttpServletResponse response) {
        AuthSession authSession = sessionRepository.findActiveSessionById(sid, now)
                .orElseThrow(() -> new SessionException(SessionErrorStatus.SESSION_EXPIRED));

        if (Objects.equals(jwtProvider.hashRT(rtPlain), authSession.getRtHashCurrent())) {
            String newPlainRT = jwtProvider.generateOpaqueRT();
            setCookie(response, newPlainRT, sid);

            String newHashRT = jwtProvider.hashRT(newPlainRT);
            String prevHashRT = jwtProvider.hashRT(rtPlain);
            setAccessTokenToHeader(authSession.getUserId(), sid, response);
            authSession.refresh(newHashRT, prevHashRT);
            return;
        }
        sessionRepository.deleteById(sid);
        setExpiredCookie(response, "__HOST-RT");
        setExpiredCookie(response, "__HOST-SID");
        SecurityContextHolder.clearContext();
        throw new SessionException(SessionErrorStatus.SESSION_EXPIRED);
    }

    private void setExpiredCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie expiredCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }


    private void setCookie(HttpServletResponse response, String refreshTokenPlain, String sessionId) {
        ResponseCookie rt = ResponseCookie.from("__HOST-RT", refreshTokenPlain)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(5))
                .build();
        ResponseCookie sid = ResponseCookie.from("__HOST-SID", sessionId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, sid.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rt.toString());
    }

    private String getClientRealIp(HttpServletRequest request) {
        // 1. X-Forwarded-For (프록시/로드밸런서)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        // 2. X-Real-IP (Nginx)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        // 3. CF-Connecting-IP (Cloudflare)
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }
        // 4. Remote Address (직접 연결)
        return request.getRemoteAddr();
    }
}


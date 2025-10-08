package com.cheolhyeon.diary.app.sse;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseEmitterController {
    private final SseEmitterService sseEmitterService;
    private final FriendRequestRepository friendRequestRepository;

    @GetMapping(value = "/subscribe/notification", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> subscribe(@CurrentUser CustomUserPrincipal user) {
        String sessionId = user.getSessionId();
        Long userId = user.getUserId();

        try {
            SseEmitter subscribe = sseEmitterService.subscribe(sessionId, userId);

            Long pendingCount = friendRequestRepository.countByRecipientId(userId, FriendRequestStatus.PENDING.name());
            sseEmitterService.sendToSid(sessionId, "pending-count", pendingCount);
            int currentActiveConnectCount = sseEmitterService.getActiveConnectionCount();
            log.debug("current active connection count: {}", currentActiveConnectCount);
            System.out.println("currentActiveConnectCount = " + currentActiveConnectCount);
            return ResponseEntity.ok(subscribe);
            
        } catch (IllegalStateException e) {
            log.warn("SSE subscription failed for user {}: {}", userId, e.getMessage());
            // 연결 수 제한 초과 시 429 Too Many Requests
            if (e.getMessage().contains("연결만 허용")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of(
                                "error", "CONNECTION_LIMIT_EXCEEDED",
                                "message", e.getMessage(),
                                "currentConnections", sseEmitterService.getUserConnectionCount(userId),
                                "maxConnections", 2
                        ));
            }
            // 서버 연결 한도 도달 시 503 Service Unavailable
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "SERVICE_UNAVAILABLE",
                            "message", e.getMessage()
                    ));
        }
    }
}

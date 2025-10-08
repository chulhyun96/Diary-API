package com.cheolhyeon.diary.app.sse;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseEmitterController {
    private final SseEmitterService sseEmitterService;
    private final FriendRequestRepository friendRequestRepository;

    @GetMapping(value = "/subscribe/notification", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@CurrentUser CustomUserPrincipal user) {
        String sessionId = user.getSessionId();
        Long userId = user.getUserId();

        SseEmitter subscribe = sseEmitterService.subscribe(sessionId, userId);

        Long pendingCount = friendRequestRepository.countByRecipientId(userId, FriendRequestStatus.PENDING.name());
        sseEmitterService.sendToSid(sessionId, "pending-count", pendingCount);
        int currentActiveConnectCount = sseEmitterService.getActiveConnectionCount();
        log.debug("current active connection count: {}", currentActiveConnectCount);
        System.out.println("currentActiveConnectCount = " + currentActiveConnectCount);
        return subscribe;
    }
}

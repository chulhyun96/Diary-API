package com.cheolhyeon.diary.app.sse;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseEmitterController {
    private final SseEmitterService sseEmitterService;
    private final FriendRequestRepository friendRequestRepository;

    @GetMapping(value = "/subscribe/notification", produces = "text/event-stream")
    public SseEmitter subscribe(@CurrentUser CustomUserPrincipal user) {
        String sessionId = user.getSessionId();
        Long userId = user.getUserId();

        SseEmitter subscribe = sseEmitterService.subscribe(sessionId);

        Long pendingCount = friendRequestRepository.countByRecipientId(userId, FriendRequestStatus.PENDING.name());
        sseEmitterService.sendToSid(sessionId, "pending-count", pendingCount);
        return subscribe;
    }
}

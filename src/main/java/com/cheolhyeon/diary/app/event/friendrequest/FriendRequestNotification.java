package com.cheolhyeon.diary.app.event.friendrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestNotification {
    private final String sessionId; // 대상자의 SessionId -> SseEmitter의 Key로 사용
    private final String requestId; // tx_id
    private final Long shareCodeOwnerUserId; // 대상자
    private final Long requesterUserId; // 요청자
    private final String requesterDisplayName; // 요청자 이름
}

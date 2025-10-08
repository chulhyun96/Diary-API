package com.cheolhyeon.diary.app.event.friendrequest;

import com.cheolhyeon.diary.app.notification.enums.EventType;
import com.cheolhyeon.diary.app.notification.enums.NotificationStatus;
import com.cheolhyeon.diary.app.notification.repository.NotificationLogRepository;
import com.cheolhyeon.diary.app.sse.SseEmitterService;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestEventListener {
    private final NotificationLogRepository notificationLogRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final SseEmitterService sseEmitterService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommit(FriendRequestNotification notification) {
        String txId = notification.getRequestId();
        Long recipientId = notification.getShareCodeOwnerUserId();

        try {
            int result = notificationLogRepository.upsertNewAttempt(txId, recipientId, EventType.FRIEND_REQUEST.name());
            if (result == 1) {
                log.warn("UPSERT FRIEND_REQUEST_PENDING 성공");
            } else if (result == 2) {
                log.warn("UPSERT FRIEND_REQUEST_PENDING 실패");
            }

            // 요청알림과 친구요청작업 자체가 서로 다른 개별 트랜잭션이기 때문에 사용자가 친구요청에 대해서 다른 쓰레드에서 발송이 가기전 처리할 수도 있기 때문에
            // 친구요청 트랜잭션과 onCommit 트랜잭션은 완전히 별개. 알림이 먼저 가지 않아도, 수락/거절은 가능한것.
            if (!friendRequestRepository.existsPendingByRequestId(txId,FriendRequestStatus.PENDING.name())) {
                notificationLogRepository.updateStatus(
                        txId, EventType.FRIEND_REQUEST.name(), NotificationStatus.IGNORE.name(),
                        "이미 처리된 알림입니다. No More PENDING");
                return;
            }
            //친구 요청 시 쿼리 호출 후 확인해야 하는 알림 개수 반환
            Long pendingCount = friendRequestRepository.countByRecipientId(recipientId, FriendRequestStatus.PENDING.name());
            sseEmitterService.sendToSid(notification.getSessionId(), "pending-count", pendingCount);

            // 알림 발송 성공 시
            notificationLogRepository.updateStatus(txId, EventType.FRIEND_REQUEST.name(), NotificationStatus.CHECKED.name(), null);
        } catch (Exception e) {
            // 알림 발송 실패 시
            log.warn("FRIEND REQUEST ERROR OCCURE MESSAGE : {}", e.getMessage());
            notificationLogRepository.updateStatus(
                    txId, EventType.FRIEND_REQUEST.name(), NotificationStatus.ERROR.name(),
                    "알림 발송중 에러 발생 트랜잭션 취소 - 알림발송 취소");
        }
    }
}

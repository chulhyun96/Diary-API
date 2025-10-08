package com.cheolhyeon.diary.app.sse;

import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.repository.FriendRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseEmitterController 단위 테스트")
class SseEmitterControllerTest {

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private SseEmitterController sseEmitterController;

    @Test
    @DisplayName("SSE 구독 성공 - pending count 0건")
    void subscribe_Success_WithZeroPendingCount() {
        // Given
        Long userId = 1L;
        String sessionId = "test-session-id";
        CustomUserPrincipal user = new CustomUserPrincipal(userId, sessionId);

        SseEmitter mockEmitter = new SseEmitter();
        given(sseEmitterService.subscribe(sessionId)).willReturn(mockEmitter);
        given(friendRequestRepository.countByRecipientId(userId, FriendRequestStatus.PENDING.name()))
                .willReturn(0L);

        // When
        SseEmitter result = sseEmitterController.subscribe(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockEmitter);

        verify(sseEmitterService).subscribe(sessionId);
        verify(friendRequestRepository).countByRecipientId(userId, FriendRequestStatus.PENDING.name());
        verify(sseEmitterService).sendToSid(sessionId, "pending-count", 0L);
    }

    @Test
    @DisplayName("SSE 구독 성공 - pending count 5건")
    void subscribe_Success_WithMultiplePendingCount() {
        // Given
        Long userId = 100L;
        String sessionId = "session-100";
        CustomUserPrincipal user = new CustomUserPrincipal(userId, sessionId);

        SseEmitter mockEmitter = new SseEmitter();
        given(sseEmitterService.subscribe(sessionId)).willReturn(mockEmitter);
        given(friendRequestRepository.countByRecipientId(userId, FriendRequestStatus.PENDING.name()))
                .willReturn(5L);

        // When
        SseEmitter result = sseEmitterController.subscribe(user);

        // Then
        assertThat(result).isNotNull();
        verify(sseEmitterService).subscribe(sessionId);
        verify(friendRequestRepository).countByRecipientId(userId, FriendRequestStatus.PENDING.name());
        verify(sseEmitterService).sendToSid(sessionId, "pending-count", 5L);
    }

    @Test
    @DisplayName("SSE 구독 성공 - 서비스 메서드 호출 순서 검증")
    void subscribe_Success_VerifyMethodCallOrder() {
        // Given
        Long userId = 200L;
        String sessionId = "session-200";
        CustomUserPrincipal user = new CustomUserPrincipal(userId, sessionId);

        SseEmitter mockEmitter = new SseEmitter();
        given(sseEmitterService.subscribe(anyString())).willReturn(mockEmitter);
        given(friendRequestRepository.countByRecipientId(anyLong(), anyString())).willReturn(3L);

        // When
        sseEmitterController.subscribe(user);

        // Then
        verify(sseEmitterService).subscribe(sessionId);
        verify(friendRequestRepository).countByRecipientId(userId, FriendRequestStatus.PENDING.name());
        verify(sseEmitterService).sendToSid(sessionId, "pending-count", 3L);
    }

    @Test
    @DisplayName("SSE 구독 - 다양한 사용자 테스트")
    void subscribe_WithDifferentUsers() {
        // Given
        Long userId1 = 1L;
        String sessionId1 = "session-1";
        CustomUserPrincipal user1 = new CustomUserPrincipal(userId1, sessionId1);

        Long userId2 = 2L;
        String sessionId2 = "session-2";
        CustomUserPrincipal user2 = new CustomUserPrincipal(userId2, sessionId2);

        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        given(sseEmitterService.subscribe(sessionId1)).willReturn(emitter1);
        given(sseEmitterService.subscribe(sessionId2)).willReturn(emitter2);
        given(friendRequestRepository.countByRecipientId(userId1, FriendRequestStatus.PENDING.name()))
                .willReturn(1L);
        given(friendRequestRepository.countByRecipientId(userId2, FriendRequestStatus.PENDING.name()))
                .willReturn(2L);

        // When
        SseEmitter result1 = sseEmitterController.subscribe(user1);
        SseEmitter result2 = sseEmitterController.subscribe(user2);

        // Then
        assertThat(result1).isEqualTo(emitter1);
        assertThat(result2).isEqualTo(emitter2);

        verify(sseEmitterService).subscribe(sessionId1);
        verify(sseEmitterService).subscribe(sessionId2);
        verify(sseEmitterService).sendToSid(sessionId1, "pending-count", 1L);
        verify(sseEmitterService).sendToSid(sessionId2, "pending-count", 2L);
    }
}



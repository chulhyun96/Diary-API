package com.cheolhyeon.diary.app.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SseEmitterService 단위 테스트")
class SseEmitterServiceTest {

    private SseEmitterService sseEmitterService;

    @BeforeEach
    void setUp() {
        sseEmitterService = new SseEmitterService();
    }

    @Test
    @DisplayName("새로운 세션 구독 성공")
    void subscribe_Success_NewSession() {
        // Given
        String sessionId = "test-session-1";
        Long userId = 1L;

        // When
        SseEmitter emitter = sseEmitterService.subscribe(sessionId, userId);

        // Then
        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(550000L);
    }

    @Test
    @DisplayName("중복 세션 구독 - 기존 연결 제거 후 새 연결 생성")
    void subscribe_Success_DuplicateSession() {
        // Given
        String sessionId = "duplicate-session";
        Long userId = 1L;

        // When
        SseEmitter firstEmitter = sseEmitterService.subscribe(sessionId, userId);
        SseEmitter secondEmitter = sseEmitterService.subscribe(sessionId, userId);

        // Then
        assertThat(firstEmitter).isNotNull();
        assertThat(secondEmitter).isNotNull();
        assertThat(firstEmitter).isNotEqualTo(secondEmitter);
    }

    @Test
    @DisplayName("다양한 세션 동시 구독 - 2개까지 허용")
    void subscribe_Success_MultipleSession() {
        // Given
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        Long userId = 1L;

        // When
        SseEmitter emitter1 = sseEmitterService.subscribe(sessionId1, userId);
        SseEmitter emitter2 = sseEmitterService.subscribe(sessionId2, userId);

        // Then
        assertThat(emitter1).isNotNull();
        assertThat(emitter2).isNotNull();
        assertThat(emitter1).isNotEqualTo(emitter2);
        
        // 3번째 연결은 제한에 걸려야 함
        assertThatThrownBy(() -> sseEmitterService.subscribe("session-3", userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 2개의 연결만 허용됩니다.");
    }

    @Test
    @DisplayName("이벤트 전송 성공 - 구독된 세션")
    void sendToSid_Success_ExistingSession() {
        // Given
        String sessionId = "active-session";
        String eventName = "test-event";
        String payload = "test-payload";
        Long userId = 1L;

        sseEmitterService.subscribe(sessionId, userId);

        // When & Then
        assertThatCode(() -> sseEmitterService.sendToSid(sessionId, eventName, payload))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이벤트 전송 - 구독하지 않은 세션 (스킵)")
    void sendToSid_Skip_NonExistingSession() {
        // Given
        String nonExistingSessionId = "non-existing-session";
        String eventName = "test-event";
        Object payload = "test-payload";

        // When & Then
        assertThatCode(() -> sseEmitterService.sendToSid(nonExistingSessionId, eventName, payload))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("pending-count 이벤트 전송 성공")
    void sendToSid_Success_PendingCountEvent() {
        // Given
        String sessionId = "user-session";
        String eventName = "pending-count";
        Long pendingCount = 5L;
        Long userId = 1L;

        sseEmitterService.subscribe(sessionId, userId);

        // When & Then
        assertThatCode(() -> sseEmitterService.sendToSid(sessionId, eventName, pendingCount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다양한 payload 타입 전송")
    void sendToSid_Success_VariousPayloadTypes() {
        // Given
        String sessionId = "payload-test-session";
        Long userId = 1L;
        sseEmitterService.subscribe(sessionId, userId);

        // When & Then
        assertThatCode(() -> {
            sseEmitterService.sendToSid(sessionId, "string-event", "string-payload");
            sseEmitterService.sendToSid(sessionId, "number-event", 123);
            sseEmitterService.sendToSid(sessionId, "long-event", 999L);
            sseEmitterService.sendToSid(sessionId, "boolean-event", true);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연결 제거 성공")
    void removeConnection_Success() {
        // Given
        String sessionId = "remove-test-session";
        Long userId = 1L;
        sseEmitterService.subscribe(sessionId, userId);

        // When
        SseEmitter removed = sseEmitterService.removeConnection(sessionId);

        // Then
        assertThat(removed).isNotNull();
    }

    @Test
    @DisplayName("연결 제거 - 존재하지 않는 세션")
    void removeConnection_NonExistingSession() {
        // Given
        String nonExistingSessionId = "non-existing-session";

        // When
        SseEmitter removed = sseEmitterService.removeConnection(nonExistingSessionId);

        // Then
        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("연결 제거 후 이벤트 전송 - 스킵됨")
    void sendToSid_Skip_AfterRemoveConnection() {
        // Given
        String sessionId = "removed-session";
        Long userId = 1L;
        sseEmitterService.subscribe(sessionId, userId);
        sseEmitterService.removeConnection(sessionId);

        // When & Then
        assertThatCode(() -> sseEmitterService.sendToSid(sessionId, "test-event", "test-data"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("구독 시 connect 이벤트 자동 전송")
    void subscribe_AutoSendConnectEvent() {
        // Given
        String sessionId = "connect-test-session";
        Long userId = 1L;

        // When
        SseEmitter emitter = sseEmitterService.subscribe(sessionId, userId);

        // Then
        assertThat(emitter).isNotNull();
        // connect 이벤트가 자동으로 전송되므로 예외 없이 완료되어야 함
    }

    @Test
    @DisplayName("동일 세션 재구독 - 기존 세션 정리 확인")
    void subscribe_CleanupOldSession() {
        // Given
        String sessionId = "resubscribe-session";
        Long userId = 1L;

        // When
        SseEmitter firstEmitter = sseEmitterService.subscribe(sessionId, userId);
        // 첫 번째 이벤트 전송 성공
        assertThatCode(() -> sseEmitterService.sendToSid(sessionId, "test", "data"))
                .doesNotThrowAnyException();

        // 재구독
        SseEmitter secondEmitter = sseEmitterService.subscribe(sessionId, userId);

        // Then
        assertThat(secondEmitter).isNotNull();
        assertThat(firstEmitter).isNotEqualTo(secondEmitter);
        // 새 emitter로 이벤트 전송 성공
        assertThatCode(() -> sseEmitterService.sendToSid(sessionId, "test2", "data2"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("여러 세션 동시 이벤트 전송 - 2개 세션")
    void sendToSid_MultipleSessionsConcurrently() {
        // Given
        String session1 = "session-1";
        String session2 = "session-2";
        Long userId = 1L;

        sseEmitterService.subscribe(session1, userId);
        sseEmitterService.subscribe(session2, userId);

        // When & Then
        assertThatCode(() -> {
            sseEmitterService.sendToSid(session1, "event1", "data1");
            sseEmitterService.sendToSid(session2, "event2", "data2");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자별 연결 수 제한 테스트 - 2개까지 허용")
    void subscribe_UserConnectionLimit_AllowTwoConnections() {
        // Given
        Long userId = 1L;
        String session1 = "session-1";
        String session2 = "session-2";

        // When & Then - 2개까지는 허용되어야 함
        assertThatCode(() -> {
            sseEmitterService.subscribe(session1, userId);
            sseEmitterService.subscribe(session2, userId);
        }).doesNotThrowAnyException();

        assertThat(sseEmitterService.getUserConnectionCount(userId)).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자별 연결 수 제한 테스트 - 3개째 연결 시 예외")
    void subscribe_UserConnectionLimit_ThirdConnectionThrowsException() {
        // Given
        Long userId = 1L;
        String session1 = "session-1";
        String session2 = "session-2";
        String session3 = "session-3";

        // When - 2개 연결 생성
        sseEmitterService.subscribe(session1, userId);
        sseEmitterService.subscribe(session2, userId);

        // Then - 3번째 연결 시 예외 발생
        assertThatThrownBy(() -> sseEmitterService.subscribe(session3, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 2개의 연결만 허용됩니다.");
    }

    @Test
    @DisplayName("사용자별 연결 수 조회 테스트")
    void getUserConnectionCount_Success() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        String session1 = "session-1";
        String session2 = "session-2";

        // When
        sseEmitterService.subscribe(session1, userId1);
        sseEmitterService.subscribe(session2, userId2);

        // Then
        assertThat(sseEmitterService.getUserConnectionCount(userId1)).isEqualTo(1);
        assertThat(sseEmitterService.getUserConnectionCount(userId2)).isEqualTo(1);
        assertThat(sseEmitterService.getUserConnectionCount(999L)).isEqualTo(0); // 존재하지 않는 사용자
    }

    @Test
    @DisplayName("전체 활성 연결 수 조회 테스트")
    void getActiveConnectionCount_Success() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        String session1 = "session-1";
        String session2 = "session-2";

        // When
        assertThat(sseEmitterService.getActiveConnectionCount()).isEqualTo(0);
        
        sseEmitterService.subscribe(session1, userId1);
        assertThat(sseEmitterService.getActiveConnectionCount()).isEqualTo(1);
        
        sseEmitterService.subscribe(session2, userId2);
        assertThat(sseEmitterService.getActiveConnectionCount()).isEqualTo(2);

        // Then
        sseEmitterService.removeConnection(session1);
        assertThat(sseEmitterService.getActiveConnectionCount()).isEqualTo(1);
    }
}


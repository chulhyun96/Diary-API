package com.cheolhyeon.diary.app.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {
    private final Map<String, SseEmitter> emitterManage = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSendTime = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userConnectionCount = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>(); // sessionId -> userId 매핑

    private static final long TIMEOUT = 550000;
    private static final long HEARTBEAT_INTERVAL = 15000;
    private static final long HEARTBEAT_THRESHOLD = 25000;

    private static final int MAX_CONNECTIONS_PER_USER = 2;
    private static final int MAX_TOTAL_CONNECTIONS = 1000;  // 전체 최대 연결 수

    public SseEmitter subscribe(String sessionId, Long userId) {
        // 전체 연결 수 체크 -> 이벤트 스트림을 서버 메모리가 아닌, Redis나 Kafka, RabbitMQ로 옮겨야 될 수도 있음.
        if (emitterManage.size() >= MAX_TOTAL_CONNECTIONS) {
            log.warn("Maximum total connections reached: {}", MAX_TOTAL_CONNECTIONS);
            throw new IllegalStateException("서버 연결 한도에 도달했습니다. 잠시 후 다시 시도해주세요.");
        }

        // 사용자별 연결 수 체크
        Integer currentUserConnections = userConnectionCount.getOrDefault(userId, 0);
        if (currentUserConnections >= MAX_CONNECTIONS_PER_USER) {
            log.warn("User {} exceeded connection limit: {}/{} (Single Session Policy)", 
                    userId, currentUserConnections, MAX_CONNECTIONS_PER_USER);
            throw new IllegalStateException("최대 " + MAX_CONNECTIONS_PER_USER + "개의 연결만 허용됩니다.");
        }
        
        // 기존 연결 정리
        SseEmitter old = emitterManage.remove(sessionId);
        if (old != null) {
            old.complete();
            lastSendTime.remove(sessionId);
            sessionToUserId.remove(sessionId);
            decrementUserConnectionCount(userId);
        }
        
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterManage.put(sessionId, emitter);
        sessionToUserId.put(sessionId, userId); // sessionId -> userId 매핑 저장
        incrementUserConnectionCount(userId);

        // Cleanup 콜백 - 모든 정리 작업 포함
        Runnable cleanup = () -> {
            emitterManage.remove(sessionId, emitter);
            lastSendTime.remove(sessionId);
            sessionToUserId.remove(sessionId);
            decrementUserConnectionCount(userId);
        };
        
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(throwable -> {
            log.warn("SSE error for session {}: {}", sessionId, throwable.getMessage());
            cleanup.run();
        });

        safeSend(emitter, SseEmitter.event()
                .name("connect")
                .reconnectTime(5000)
                .data("ok")
        );
        updateLastSendTime(sessionId);
        
        log.info("SSE connected: session={}, user={}, total={}, userConnections={}", 
                sessionId, userId, emitterManage.size(), currentUserConnections);
        
        return emitter;
    }

    private void incrementUserConnectionCount(Long userId) {
        userConnectionCount.merge(userId, 1, Integer::sum);
    }

    private void decrementUserConnectionCount(Long userId) {
        userConnectionCount.compute(userId, (k, v) -> {
            if (v == null || v <= 1) {
                return null; // 0이면 Map에서 제거
            }
            return v - 1;
        });
    }

    public void updateLastSendTime(String sessionId) {
        lastSendTime.put(sessionId, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        if (emitterManage.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        int heartbeatSent = 0;

        for (Map.Entry<String, SseEmitter> entry : emitterManage.entrySet()) {
            String sessionId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            Long lastSend = lastSendTime.get(sessionId);
            if (lastSend == null || (now - lastSend) > HEARTBEAT_THRESHOLD) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                    updateLastSendTime(sessionId);
                    heartbeatSent++;
                } catch (IOException e) {
                    log.warn("Failed to send heartbeat to session: {}", sessionId);
                    emitter.completeWithError(e);
                    // Heartbeat 실패 시 연결 정리
                    emitterManage.remove(sessionId, emitter);
                    lastSendTime.remove(sessionId);
                    Long userId = sessionToUserId.remove(sessionId);
                    if (userId != null) {
                        decrementUserConnectionCount(userId);
                    }
                }
            }
        }
        if (heartbeatSent > 0) {
            log.debug("Sent heartbeat to {}/{} connections",
                    heartbeatSent, emitterManage.size());
        }
    }

    @Scheduled(fixedRate = 60000)
    public void logConnectionStats() {
        log.info("Active SSE connections: {}", emitterManage.size());
        userConnectionCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> log.debug("User {}: {} connections", entry.getKey(), entry.getValue()));
    }

    public void sendToSid(String sessionId, String eventName, Object payload) {
        SseEmitter emitter = emitterManage.get(sessionId);
        if (emitter == null) {
            return; // 사용자가 구독을 안했거나 끊겼으면 스킵
        }
        safeSend(emitter, SseEmitter.event().name(eventName).data(payload));
        updateLastSendTime(sessionId);
    }


    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder data) {
        try {
            emitter.send(data);
        } catch (Exception ex) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                emitterManage.values().removeIf(it -> Objects.equals(it, emitter));
            }
        }
    }

    public SseEmitter removeConnection(String sessionId) {
        SseEmitter removed = emitterManage.remove(sessionId);
        if (removed != null) {
            lastSendTime.remove(sessionId);
            Long userId = sessionToUserId.remove(sessionId);
            if (userId != null) {
                decrementUserConnectionCount(userId);
            }
        }
        return removed;
    }

    public int getUserConnectionCount(Long userId) {
        return userConnectionCount.getOrDefault(userId, 0);
    }

     // 현재 활성 연결 수 조회 (모니터링용)
    public int getActiveConnectionCount() {
        return emitterManage.size();
    }
}

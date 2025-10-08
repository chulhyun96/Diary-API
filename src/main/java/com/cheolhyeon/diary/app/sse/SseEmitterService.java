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

    private static final long TIMEOUT = 550000;
    private static final long HEARTBEAT_INTERVAL = 15000;
    private static final long HEARTBEAT_THRESHOLD = 25000;

    public SseEmitter subscribe(String sessionId) {
        SseEmitter old = emitterManage.remove(sessionId);
        if (old != null) {
            old.complete();
        }
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterManage.put(sessionId, emitter);

        Runnable cleanup = () -> emitterManage.remove(sessionId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(throwable -> cleanup.run());

        safeSend(emitter, SseEmitter.event()
                .name("connect")
                .reconnectTime(5000)
                .data("ok")
        );
        updateLastSendTime(sessionId);
        return emitter;
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
        return emitterManage.remove(sessionId);
    }
}

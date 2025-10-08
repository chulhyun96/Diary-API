package com.cheolhyeon.diary.app.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {
    private final Map<String, SseEmitter> emitterManage = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 550000;

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
        return emitter;
    }

    public void sendToSid(String sessionId, String eventName, Object payload) {
        SseEmitter emitter = emitterManage.get(sessionId);
        if (emitter == null) {
            return; // 사용자가 구독을 안했거나 끊겼으면 스킵
        }
        safeSend(emitter, SseEmitter.event().name(eventName).data(payload));
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

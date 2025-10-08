package com.cheolhyeon.diary.app.event.session;

import com.cheolhyeon.diary.app.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {
    private final SseEmitterService emitterService;

    @EventListener
    public void handleSessionInvalidated(SessionInvalidatedEvent event) {
        SseEmitter emitter = emitterService.removeConnection(event.getSessionId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("session-invalidated")
                        .data("다른 기기에서 로그인되었습니다"));
                emitter.complete();
            } catch (Exception e) {
                log.warn("Session invalidated event failed", e);
            }
        }
    }
}

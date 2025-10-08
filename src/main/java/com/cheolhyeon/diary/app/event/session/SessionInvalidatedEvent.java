package com.cheolhyeon.diary.app.event.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionInvalidatedEvent {
    private String sessionId;
}

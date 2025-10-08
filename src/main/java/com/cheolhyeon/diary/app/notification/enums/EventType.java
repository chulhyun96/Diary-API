package com.cheolhyeon.diary.app.notification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    FRIEND_REQUEST(
            "친구요청"
    ),
    COMMENT("POST에 댓글이 달릴 경우"),
    POST("게시글 공유 태그");

    private final String description;
}

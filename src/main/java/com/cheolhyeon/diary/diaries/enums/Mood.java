package com.cheolhyeon.diary.diaries.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Mood {
    HAPPY("기쁨"),
    SAD("슬픔"),
    EXCITED("행복"),
    TIRED("피곤"),
    ANNOY("짜증"),
    NORMAL("보통");

    private final String description;
}

package com.cheolhyeon.diary.diary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Weather {
    SUNNY("맑음"),
    HAZY("흐림"),
    RAIN("비"),
    SNOW("눈"),
    MIST("안개");

    private final String description;

}

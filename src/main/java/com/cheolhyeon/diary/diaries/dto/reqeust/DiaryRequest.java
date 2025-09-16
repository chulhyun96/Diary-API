package com.cheolhyeon.diary.diaries.dto.reqeust;

import com.cheolhyeon.diary.diaries.enums.Mood;
import com.cheolhyeon.diary.diaries.enums.Weather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryRequest {
    private Long writerId;
    private String writer;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private String location;
    private String tags;
}

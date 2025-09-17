package com.cheolhyeon.diary.diary.dto.reqeust;

import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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


    public static Diaries toEntity(Long kakaoId, String displayName, List<String> s3Key, DiaryRequest request) {
        return new Diaries(kakaoId, displayName, request.getTitle(),
                request.getContent(), request.getMood(), request.getWeather(),
                request.getLocation(), request.getTags(), s3Key,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }
}

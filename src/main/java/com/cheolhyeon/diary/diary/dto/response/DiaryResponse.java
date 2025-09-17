package com.cheolhyeon.diary.diary.dto.response;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponse {
    private String displayName;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private List<String> imagesJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiaryResponse toResponse(Diaries entity) {
        return new DiaryResponse(
                entity.getWriter(), entity.getTitle(), entity.getContent(),
                entity.getMood(), entity.getWeather(), entity.getImageKeysJson(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}

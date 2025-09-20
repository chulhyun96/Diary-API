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
public class DiaryResponseById {
    private byte[] diaryId;
    private String writer;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private Location location;
    private List<String> tags;
    private List<String> imagesJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiaryResponseById toResponse(Diaries diary, List<String> imageUrl) {
        return new DiaryResponseById(
                diary.getDiaryId(),
                diary.getWriter(),
                diary.getTitle(),
                diary.getContent(),
                diary.getMood(),
                diary.getWeather(),
                diary.getLocation(),
                diary.getTagsJson(),
                imageUrl,
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}

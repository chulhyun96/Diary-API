package com.cheolhyeon.diary.diary.dto.response;

import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
import com.github.f4b6a3.ulid.Ulid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponseByYearAndMonth {
    private String diaryIdString;
    private String displayName;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static List<DiaryResponseByYearAndMonth> toResponse(List<Diaries> allDiariesByYearAndMonth, List<String> thumbnailImage) {
        // 나중에 코드 리팩토링 해야할듯.
        List<DiaryResponseByYearAndMonth> diaryResponses = new ArrayList<>();
        for (int i = 0; i < allDiariesByYearAndMonth.size(); i++) {
            Diaries diaries = allDiariesByYearAndMonth.get(i);
            if (diaries.getDeletedAt() != null) {
                continue;
            }
            String thumbnailUrl = i < thumbnailImage.size() ? thumbnailImage.get(i) : null;
            String diaryIdAsString = Ulid.from(diaries.getDiaryId()).toString();
            diaryResponses.add(new DiaryResponseByYearAndMonth(
                    diaryIdAsString,
                    diaries.getWriter(),
                    diaries.getTitle(),
                    diaries.getContent(),
                    diaries.getMood(),
                    diaries.getWeather(),
                    thumbnailUrl,
                    diaries.getCreatedAt(),
                    diaries.getUpdatedAt()
            ));
        }
        return diaryResponses;
    }
}

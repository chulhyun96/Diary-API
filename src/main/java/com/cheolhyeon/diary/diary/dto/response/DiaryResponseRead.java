package com.cheolhyeon.diary.diary.dto.response;

import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
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
public class DiaryResponseRead {
    private byte[] diaryId;
    private String displayName;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static List<DiaryResponseRead> toResponse(List<Diaries> diariesByMonth, List<String> thumbnailImage) {
        // 나중에 코드 리팩토링 해야할듯.
        List<DiaryResponseRead> diaryResponses = new ArrayList<>();
        for (int i = 0; i < diariesByMonth.size(); i++) {
            Diaries diaries = diariesByMonth.get(i);
            String thumbnailUrl = i < thumbnailImage.size() ? thumbnailImage.get(i) : null;
            
            diaryResponses.add(new DiaryResponseRead(
                    diaries.getDiaryId(), diaries.getWriter(),
                    diaries.getTitle(), diaries.getContent(),
                    diaries.getMood(), diaries.getWeather(),
                    thumbnailUrl, diaries.getCreatedAt(), diaries.getUpdatedAt()));
        }
        return diaryResponses;
    }
}

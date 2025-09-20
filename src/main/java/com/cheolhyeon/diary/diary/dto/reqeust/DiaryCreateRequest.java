package com.cheolhyeon.diary.diary.dto.reqeust;

import com.cheolhyeon.diary.diary.dto.response.Location;
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
public class DiaryCreateRequest {
    private String writer;
    private String title;
    private String content;
    private Mood mood;
    private Weather weather;
    private Location location;
    private List<String> tags;


    public static Diaries toEntity(byte[] diaryId, Long writerId, String displayName, List<String> s3Key, DiaryCreateRequest request) {
        return Diaries.builder()
                .diaryId(diaryId)
                .writerId(writerId)
                .writer(displayName)
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .weather(request.getWeather())
                .location(request.getLocation())
                .tagsJson(request.getTags())
                .imageKeysJson(s3Key)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
    }
}

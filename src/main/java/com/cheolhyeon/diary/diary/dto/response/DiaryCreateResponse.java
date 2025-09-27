package com.cheolhyeon.diary.diary.dto.response;

import com.cheolhyeon.diary.diary.entity.Diaries;
import com.github.f4b6a3.ulid.Ulid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryCreateResponse {
    private String diaryId;
    private int year;
    private int month;
    private int day;

    public static DiaryCreateResponse toResponse(Diaries savedEntity) {
        LocalDateTime createdAt = savedEntity.getCreatedAt();
        String diaryId = Ulid.from(savedEntity.getDiaryId()).toString();
        return DiaryCreateResponse.builder()
                .diaryId(diaryId)
                .year(createdAt.getYear())
                .month(createdAt.getMonthValue())
                .day(createdAt.getDayOfMonth())
                .build();
    }
}

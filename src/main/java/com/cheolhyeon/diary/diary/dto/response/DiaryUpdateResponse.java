package com.cheolhyeon.diary.diary.dto.response;

import com.cheolhyeon.diary.diary.entity.Diaries;
import com.github.f4b6a3.ulid.Ulid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateResponse {
    private String diaryId;
    private String title;
    private String content;
    private int imageCount;
    private LocalDateTime updatedAt;

    public static DiaryUpdateResponse toResponse(Diaries savedEntity) {
        String diaryId = Ulid.from(savedEntity.getDiaryId()).toString();
        return DiaryUpdateResponse.builder()
                .diaryId(diaryId)
                .title(savedEntity.getTitle())
                .content(savedEntity.getContent())
                .imageCount(Optional.ofNullable(savedEntity.getImageKeysJson())
                        .orElseGet(List::of)
                        .size())
                .updatedAt(savedEntity.getUpdatedAt())
                .build();
    }
}

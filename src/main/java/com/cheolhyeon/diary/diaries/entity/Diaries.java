package com.cheolhyeon.diary.diaries.entity;

import com.cheolhyeon.diary.diaries.enums.Mood;
import com.cheolhyeon.diary.diaries.enums.Weather;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diaries {
    @Id
    private Long writerId;
    private String writer;
    private String title;
    private String content;
    @Enumerated(EnumType.STRING)
    private Mood mood;
    @Enumerated(EnumType.STRING)
    private Weather weather;
    private String location;
    private String tags;
    private String coverImageKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}

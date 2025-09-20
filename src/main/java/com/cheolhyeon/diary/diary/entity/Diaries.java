package com.cheolhyeon.diary.diary.entity;

import com.cheolhyeon.diary.diary.dto.response.Location;
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diaries {
    @Id
    @Column(columnDefinition = "binary(16)")
    private byte[] diaryId;
    private Long writerId;
    private String writer;
    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private Mood mood;

    @Enumerated(EnumType.STRING)
    private Weather weather;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Location location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> tagsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> imageKeysJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}

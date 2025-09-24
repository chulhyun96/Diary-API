package com.cheolhyeon.diary.diary.dto.reqeust;

import com.cheolhyeon.diary.diary.enums.Mood;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryUpdateRequest {
    private String title;
    private String content;
    private Mood mood;
    private List<String> tags;
}

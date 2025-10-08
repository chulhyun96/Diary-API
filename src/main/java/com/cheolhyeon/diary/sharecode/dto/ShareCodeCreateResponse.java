package com.cheolhyeon.diary.sharecode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareCodeCreateResponse {
    private int statusCode;
    private String codePlain;
    private String message;
}

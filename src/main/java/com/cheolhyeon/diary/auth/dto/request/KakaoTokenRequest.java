package com.cheolhyeon.diary.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenRequest {
    private String grantType;
    private String clientId;
    private String redirectUri;
    private String code;
    private String clientSecret;
}

package com.cheolhyeon.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class JWTResponse {
    private boolean success;
    private String accessToken;
    private String oauth2Id;
    private String nickname;

    public JWTResponse(boolean success, String accessToken, String oauth2Id, String nickname) {
        this.success = success;
        this.accessToken = accessToken;
        this.oauth2Id = oauth2Id;
        this.nickname = nickname;
    }
}

package com.cheolhyeon.diary.token.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private LocalDateTime expiresAt;

    public static JwtResponse createJwtResponse(String accessToken, LocalDateTime expiresAt) {
        return new JwtResponse(accessToken, expiresAt);
    }


}

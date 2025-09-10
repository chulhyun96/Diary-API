package com.cheolhyeon.diary.token.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtRequest {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private Date createDate;
    private Long refreshExpireDate;
}

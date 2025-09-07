package com.cheolhyeon.diary.auth.service;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.app.feign.external.KakaoApiClient;
import com.cheolhyeon.diary.app.feign.external.KakaoTokenClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;
    @Value("${kakao.client.redirect_url}")
    private String redirectUrl;

    public KakaoUserInfoResponse getKakaoLoginCode(String code) {
        String contentType = "application/x-www-form-urlencoded;charset=UTF-8";
        KakaoTokenRequest authKakaoServer = KakaoTokenRequest.builder()
                .client_id(kakaoClientId)
                .client_secret(kakaoClientSecret)
                .code(code)
                .grant_type("authorization_code")
                .redirect_uri(redirectUrl)
                .build();
        KakaoTokenResponse token = kakaoTokenClient.getToken(contentType, authKakaoServer);
        return kakaoApiClient.getMe("Bearer " + token.getAccess_token());
    }

    public String getLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUrl
                + "&response_type=code";
    }
}

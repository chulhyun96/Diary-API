package com.cheolhyeon.diary.auth.component;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class KakaoApiComponent {
    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;
    @Value("${kakao.client.redirect_url}")
    private String redirectUrl;
    @Value("${kakao.client.content_type}")
    private String contentType;
    @Value("${kakao.client.kakao_auth_url}")
    private String kakaoAuthUrl;
    @Value("${kakao.client.response_type}")
    private String responseType;

    public KakaoTokenRequest getKakaoTokenRequest(String code) {
        return KakaoTokenRequest.builder()
                .client_id(kakaoClientId)
                .client_secret(kakaoClientSecret)
                .code(code)
                .grant_type("authorization_code")
                .redirect_uri(redirectUrl)
                .build();
    }
    public String buildLoginUrl() {
        try {
            return kakaoAuthUrl +
                    "?client_id=" + URLEncoder.encode(kakaoClientId, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8) +
                    "&response_type=" + responseType;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build login URL", e);
        }
    }
}

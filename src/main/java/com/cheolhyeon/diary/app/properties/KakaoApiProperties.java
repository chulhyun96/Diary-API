package com.cheolhyeon.diary.app.properties;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao.client")
public class KakaoApiProperties {
    private String id;
    private String secret;
    private String redirectUrl;
    private String contentType;
    private String kakaoAuthUrl;
    private String responseType;


    public KakaoTokenRequest getKakaoTokenRequest(String code) {
        return KakaoTokenRequest.builder()
                .client_id(id)
                .client_secret(secret)
                .code(code)
                .grant_type("authorization_code")
                .redirect_uri(redirectUrl)
                .build();
    }

    public String buildLoginUrl() {
        try {
            return kakaoAuthUrl +
                    "?client_id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8) +
                    "&response_type=" + responseType;
        } catch (Exception e) {
            //TODO : 이 때는 에러를 던지기 전에 리트라이 로직이라던가, 로깅작업을 진행한 후에 에러를 던지고, 스택트레이스를 콘솔에 찍는걸로
            throw new RuntimeException("Failed to build login URL", e);
        }
    }
}

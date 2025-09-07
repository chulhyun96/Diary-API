package com.cheolhyeon.diary.auth.controller;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import com.cheolhyeon.diary.auth.external.KakaoTokenClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoTokenController {
    private final KakaoTokenClient kakaoTokenClient;

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;
    @Value("${kakao.client.redirect_url}")
    private String redirectUrl;

    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<?> getLoginKakaoCode(String code) {
        String contentType = "application/x-www-form-urlencoded;charset=UTF-8";
        KakaoTokenRequest authKakaoServer = KakaoTokenRequest.builder()
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .code(code)
                .grantType("authorization_code")
                .redirectUri(redirectUrl)
                .build();
        KakaoTokenResponse token = kakaoTokenClient.getToken(contentType, authKakaoServer);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUrl
                + "&response_type=code";
        return ResponseEntity.ok()
                .header("Location", url)
                .build();
    }

}

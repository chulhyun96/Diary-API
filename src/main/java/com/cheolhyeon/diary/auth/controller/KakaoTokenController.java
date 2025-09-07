package com.cheolhyeon.diary.auth.controller;

import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoTokenController {
    private final LoginService loginService;

    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<KakaoUserInfoResponse> getKakaoCode(String code) {
        KakaoUserInfoResponse me = loginService.getKakaoLoginCode(code);
        return ResponseEntity.ok(me);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> getLoginUrl() {
        String url = loginService.getLoginUrl();
        return ResponseEntity
                .status(302)
                .header("Location", url)
                .build();
    }
}

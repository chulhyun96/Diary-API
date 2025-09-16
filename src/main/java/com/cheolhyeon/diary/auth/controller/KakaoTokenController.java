package com.cheolhyeon.diary.auth.controller;

import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoTokenController {
    private final UserService userService;

    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<KakaoUserInfoResponse> getKakaoCode(String code) {
        KakaoUserInfoResponse me = userService.processLogin(code);
        return ResponseEntity.ok(me);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String url = userService.getLoginUrl();
        return ResponseEntity
                .status(302)
                .header("Location", url)
                .build();
    }
}
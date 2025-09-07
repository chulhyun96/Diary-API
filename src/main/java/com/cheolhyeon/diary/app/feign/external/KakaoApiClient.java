package com.cheolhyeon.diary.app.feign.external;

import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://kapi.kakao.com", name = "kakaoApi")
public interface KakaoApiClient {
    @GetMapping("/v2/user/me")
    KakaoUserInfoResponse getMe(@RequestHeader("Authorization") String bearerToken);
}

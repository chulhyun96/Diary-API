package com.cheolhyeon.diary.auth.external;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://kauth.kakao.com", name = "kakaoTokenClient")
public interface KakaoTokenClient {
    @PostMapping(value = "/oauth/token", consumes = "application/json")
    KakaoTokenResponse getToken(
            @RequestHeader("Content-Type") String contentType,
            @SpringQueryMap KakaoTokenRequest request
    );
}

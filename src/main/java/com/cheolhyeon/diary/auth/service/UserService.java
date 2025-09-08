package com.cheolhyeon.diary.auth.service;

import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.app.feign.external.KakaoApiClient;
import com.cheolhyeon.diary.app.feign.external.KakaoTokenClient;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;
    @Value("${kakao.client.redirect_url}")
    private String redirectUrl;

    @Transactional
    public KakaoUserInfoResponse processLogin(String code) {
        String contentType = "application/x-www-form-urlencoded;charset=UTF-8";
        KakaoTokenRequest authKakaoServer = KakaoTokenRequest.builder()
                .client_id(kakaoClientId)
                .client_secret(kakaoClientSecret)
                .code(code)
                .grant_type("authorization_code")
                .redirect_uri(redirectUrl)
                .build();
        KakaoTokenResponse token = kakaoTokenClient.getToken(contentType, authKakaoServer);
        KakaoUserInfoResponse client = kakaoApiClient.getMe("Bearer " + token.getAccess_token());

        userRepository.findById(client.getId())
                .map(existedUser -> {
                    existedUser.updateLastLoginTime();
                    return userRepository.save(existedUser);
                })
                .orElseGet(() -> {
                    User newUser = User.createUser(client);
                    return userRepository.save(newUser);
                });
        return client;
    }

    public String getLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUrl
                + "&response_type=code";
    }
}

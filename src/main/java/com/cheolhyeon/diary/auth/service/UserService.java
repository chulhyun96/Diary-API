package com.cheolhyeon.diary.auth.service;

import com.cheolhyeon.diary.auth.component.KakaoApiComponent;
import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.app.feign.external.KakaoApiClient;
import com.cheolhyeon.diary.app.feign.external.KakaoTokenClient;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;
    private final KakaoApiComponent kakaoApiComponent;

    @Transactional
    public KakaoUserInfoResponse processLogin(String code) {
        KakaoTokenRequest kakaoTokenRequest = kakaoApiComponent.getKakaoTokenRequest(code);
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenClient.getToken(kakaoApiComponent.getContentType(), kakaoTokenRequest);
        KakaoUserInfoResponse client = kakaoApiClient.getMe("Bearer " + kakaoTokenResponse.getAccess_token());

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
        return kakaoApiComponent.buildLoginUrl();
    }

}

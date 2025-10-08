package com.cheolhyeon.diary.auth.service;

import com.cheolhyeon.diary.app.feign.external.KakaoApiClient;
import com.cheolhyeon.diary.app.feign.external.KakaoTokenClient;
import com.cheolhyeon.diary.app.properties.KakaoApiProperties;
import com.cheolhyeon.diary.auth.dto.request.KakaoTokenRequest;
import com.cheolhyeon.diary.auth.dto.response.KakaoTokenResponse;
import com.cheolhyeon.diary.auth.dto.response.KakaoUserInfoResponse;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;

    private final KakaoApiProperties kakaoApiProperties;

    @Transactional
    public Long processLogin(String code) {
        KakaoTokenRequest kakaoTokenRequest = kakaoApiProperties.getKakaoTokenRequest(code);
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenClient.getToken(kakaoApiProperties.getContentType(), kakaoTokenRequest);
        KakaoUserInfoResponse client = kakaoApiClient.getMe("Bearer " + kakaoTokenResponse.getAccess_token());

        User user = userRepository.findById(client.getId())
                .map(existedUser -> {
                    existedUser.updateLastLoginTime();
                    return userRepository.save(existedUser);
                })
                .orElseGet(() -> {
                    User newUser = User.createUser(client);
                    return userRepository.save(newUser);
                });
        return user.getUserId();
    }

    public String getLoginUrl() {
        return kakaoApiProperties.buildLoginUrl();
    }
}

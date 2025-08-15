package com.cheolhyeon.diary.service;

import com.cheolhyeon.diary.entity.User;
import com.cheolhyeon.diary.repository.UserRepository;
import com.cheolhyeon.diary.security.CustomOAuth2User;
import com.cheolhyeon.diary.type.Oauth2ProviderOption;
import com.cheolhyeon.diary.util.converter.UlidConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("🔄 OAuth2 사용자 정보 로드 시작");
        OAuth2User oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        Oauth2ProviderOption providerOpt = Oauth2ProviderOption.getOption(provider);
        if (Objects.requireNonNull(providerOpt).getOption().equals(provider)) {
            log.info("OAuth2 Provider : {}", providerOpt);
            return processOauth2User(oauth2User);
        }
        throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
    }

    private OAuth2User processOauth2User(OAuth2User oauth2User) {
        log.info("🔍 OAuth2 사용자 정보 처리 시작");
        Map<String, Object> attributes = oauth2User.getAttributes();

        String oauth2Id = String.valueOf(attributes.get("id"));
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String currentNickname = properties != null ? (String) properties.get("nickname") : "null";

        return userRepository.findByOauth2Id(oauth2Id)
                .map(existingUser -> {
                    // 기존 사용자가 있는 경우
                    if (!currentNickname.equals(existingUser.getNickname())) {
                        // 닉네임이 다른 경우: 업데이트 후 저장
                        log.info("📝 닉네임 변경 감지 - 업데이트 진행");
                        existingUser.updateNickname(currentNickname);
                        User updatedUser = userRepository.save(existingUser);
                        return new CustomOAuth2User(oauth2User, updatedUser);
                    }
                    // 닉네임이 같은 경우: 바로 반환
                    log.info("✅ 닉네임 변경 없음 - 기존 정보 그대로 사용");
                    return new CustomOAuth2User(oauth2User, existingUser);
                })
                .orElseGet(() -> {
                    // 사용자가 없는 경우: 새로 생성
                    log.info("🆕 새로운 사용자 생성");
                    User newUser = User.createUser(UlidConverter.generateUlid(), oauth2Id, currentNickname);
                    User savedUser = userRepository.save(newUser);
                    log.info("💾 새 사용자 생성 완료 - 사용자 ID: {}", savedUser.getNickname());
                    return new CustomOAuth2User(oauth2User, savedUser);
                });
    }
} 
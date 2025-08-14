package com.cheolhyeon.diary.service;

import com.cheolhyeon.diary.entity.User;
import com.cheolhyeon.diary.repository.UserRepository;
import com.cheolhyeon.diary.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

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
        log.info("🔗 OAuth2 제공자: {}", provider);
        log.info("📋 OAuth2 사용자 속성: {}", oauth2User.getAttributes());
        
        if ("kakao".equals(provider)) {
            log.info("✅ 카카오 사용자 처리 시작");
            return processKakaoUser(oauth2User);
        }
        log.error("❌ 지원하지 않는 OAuth2 제공자: {}", provider);
        throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
    }

    private OAuth2User processKakaoUser(OAuth2User oauth2User) {
        log.info("🔍 카카오 사용자 정보 처리 시작");
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // 카카오 사용자 정보 추출 (nickname만)
        String kakaoId = String.valueOf(attributes.get("id"));
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        
        String nickname = properties != null ? (String) properties.get("nickname") : "Unknown";
        
        log.info("👤 카카오 사용자 정보 추출 완료:");
        log.info("   - 카카오 ID: {}", kakaoId);
        log.info("   - 닉네임: {}", nickname);

        // 사용자 정보 저장 또는 업데이트
        User user = userRepository.findByKakaoId(kakaoId)
                .orElse(User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .email(null)  // 이메일 동의하지 않음
                        .build());

        if (user.getId() != null) {
            log.info("🔄 기존 사용자 정보 업데이트 - 사용자 ID: {}", user.getId());
            user.setNickname(nickname);
            // 이메일과 프로필 이미지는 업데이트하지 않음 (동의하지 않음)
        } else {
            log.info("🆕 새로운 사용자 생성");
        }
        
        user = userRepository.save(user);
        log.info("💾 사용자 정보 저장 완료 - 사용자 ID: {}", user.getId());
        
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oauth2User, user);
        log.info("✅ CustomOAuth2User 생성 완료");
        
        return customOAuth2User;
    }
} 
package com.cheolhyeon.diary.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoResponse {
    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    public static class KakaoAccount {
        private Profile profile;
        @Getter
        public static class Profile {
            private String nickname;
        }
    }
}

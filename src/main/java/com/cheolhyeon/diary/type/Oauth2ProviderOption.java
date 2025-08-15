package com.cheolhyeon.diary.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum Oauth2ProviderOption {
    KAKAO("kakao");

    private final String option;

    Oauth2ProviderOption(String option) {
        this.option = option;
    }

    public static Oauth2ProviderOption getOption(String provider) {
        if (Objects.equals(provider, KAKAO.getOption())) {
            return KAKAO;
        }
        return null;
    }
}

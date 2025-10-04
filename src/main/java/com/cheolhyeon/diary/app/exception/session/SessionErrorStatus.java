package com.cheolhyeon.diary.app.exception.session;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public enum SessionErrorStatus implements ErrorStatus, Serializable {

    ONLY_SINGLE_SESSION(
            -1,
            "Invalid Session",
            "여러 기기에서 이용하실 수 없습니다."),
    SESSION_EXPIRED(
            -2
            ,"Session Expired"
            ,"세션이 만료되었습니다. 다시 로그인해주세요."
    );
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

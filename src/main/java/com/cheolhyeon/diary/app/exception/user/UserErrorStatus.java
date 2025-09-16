package com.cheolhyeon.diary.app.exception.user;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum UserErrorStatus implements ErrorStatus {
    NOT_FOUND(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "해당 유저는 회원이 아닙니다."
    );
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

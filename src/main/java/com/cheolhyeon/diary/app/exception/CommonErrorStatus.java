package com.cheolhyeon.diary.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum CommonErrorStatus implements ErrorStatus {
    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            "데이터 무결성 제약 조건 위반으로 인한 오류가 발생했습니다."
    );

    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

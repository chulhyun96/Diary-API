package com.cheolhyeon.diary.app.exception.hashcode;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum GenerationHashCodeErrorStatus implements ErrorStatus {
    GENERATE_FAILED_HASH_CODE(
            422,
            "FAILID_HASH_CODE",
            "Hmac 코드 암호화 생성 중 오류"
    );

    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

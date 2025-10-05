package com.cheolhyeon.diary.app.exception.sharecode;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum ShareCodeErrorStatus implements ErrorStatus {

    ONLY_SINGLE_SHARE_CODE(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "공유 코드는 계정 당 하나밖에 생성이 불가합니다. 기존의 것을 삭제해주세요"
            ),
    NOT_FOUND(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "공유 코드가 존재하지 않습니다."
    );

    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

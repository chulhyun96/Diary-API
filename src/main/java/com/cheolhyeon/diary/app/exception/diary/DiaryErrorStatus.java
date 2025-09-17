package com.cheolhyeon.diary.app.exception.diary;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum DiaryErrorStatus implements ErrorStatus {
    FAILED_SAVE(
           HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "이미지 저장 실패."
    );
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

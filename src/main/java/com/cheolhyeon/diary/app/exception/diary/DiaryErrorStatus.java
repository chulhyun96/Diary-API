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
    NOT_FOUND(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "해당 일기를 찾을 수 없습니다."
    );
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

package com.cheolhyeon.diary.app.exception.diary;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class DiaryException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public DiaryException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

package com.cheolhyeon.diary.app.exception.diary;

import com.cheolhyeon.diary.app.exception.ErrorStatus;

public class DiaryException extends RuntimeException {
    public DiaryException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
    }
}

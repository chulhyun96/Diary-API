package com.cheolhyeon.diary.app.exception.session;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class SessionException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public SessionException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

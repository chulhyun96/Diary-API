package com.cheolhyeon.diary.app.exception.user;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public UserException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

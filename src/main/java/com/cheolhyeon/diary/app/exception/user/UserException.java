package com.cheolhyeon.diary.app.exception.user;

import com.cheolhyeon.diary.app.exception.ErrorStatus;

public class UserException extends RuntimeException {

    public UserException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
    }
}

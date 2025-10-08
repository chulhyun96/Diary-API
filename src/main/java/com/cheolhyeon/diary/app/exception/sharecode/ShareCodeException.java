package com.cheolhyeon.diary.app.exception.sharecode;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class ShareCodeException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public ShareCodeException(ShareCodeErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

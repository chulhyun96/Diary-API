package com.cheolhyeon.diary.app.exception.hashcode;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class GenerationHashCodeException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public GenerationHashCodeException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

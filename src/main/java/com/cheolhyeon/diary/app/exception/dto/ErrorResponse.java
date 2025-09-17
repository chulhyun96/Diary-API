package com.cheolhyeon.diary.app.exception.dto;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;


    public static ErrorResponse of(ErrorStatus errorStatus) {
        return new ErrorResponse(
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription());
    }
}

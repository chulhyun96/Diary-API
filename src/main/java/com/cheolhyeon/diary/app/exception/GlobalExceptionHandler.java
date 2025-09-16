package com.cheolhyeon.diary.app.exception;

import com.cheolhyeon.diary.app.exception.dto.ErrorResponse;
import com.cheolhyeon.diary.app.exception.user.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ErrorResponse handleUserException(ErrorStatus errorStatus) {
        log.error("UserException ErrorCode : {},  ErrorMessage : {}, ErrorDesc : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.create(errorStatus);
    }
}

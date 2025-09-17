package com.cheolhyeon.diary.app.exception;

import com.cheolhyeon.diary.app.exception.diary.DiaryException;
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
        log.error("UserException ERRORCODE : {},  ERRORMESSGE : {}, ERRORDESCRIPTION : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(DiaryException.class)
    public ErrorResponse handleDiaryException(ErrorStatus errorStatus) {
        log.error("DiaryException ERRORCODE : {}, ERRORMESSGE : {}, ERRORDESCRIPTION : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.of(errorStatus);
    }
}

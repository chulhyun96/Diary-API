package com.cheolhyeon.diary.app.exception;

import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.dto.ErrorResponse;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
import com.cheolhyeon.diary.app.exception.user.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ErrorResponse handleUserException(UserException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        log.error("UserException ERROR CODE : {},  ERROR MESSAGE : {}, ERROR DESCRIPTION : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(DiaryException.class)
    public ErrorResponse handleDiaryException(DiaryException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        log.error("DiaryException ERROR CODE : {}, ERROR MESSAGE : {}, ERROR DESCRIPTION : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.of(errorStatus);
    }
    @ExceptionHandler(S3Exception.class)
    public ErrorResponse handleS3Exception(S3Exception e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        log.error("S3Exception ERROR CODE : {}, ERROR MESSAGE : {}, ERROR DESCRIPTION : {}",
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription()
        );
        return ErrorResponse.of(errorStatus);
    }
}

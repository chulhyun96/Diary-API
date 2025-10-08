package com.cheolhyeon.diary.app.exception;

import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.dto.ErrorResponse;
import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeException;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
import com.cheolhyeon.diary.app.exception.session.SessionException;
import com.cheolhyeon.diary.app.exception.session.UserException;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ErrorResponse handleUserException(UserException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        String errorLocation = extractErrorLocation(e.getStackTrace());
        log.error("""
                ===== UserException 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                ==============================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(DiaryException.class)
    public ErrorResponse handleDiaryException(DiaryException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        
        String errorLocation = extractErrorLocation(e.getStackTrace());
        log.error("""
                ===== DiaryException 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                ===============================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(S3Exception.class)
    public ErrorResponse handleS3Exception(S3Exception e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        String errorLocation = extractErrorLocation(e.getStackTrace());

        log.error("""
                ===== S3Exception 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                ============================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(SessionException.class)
    public ErrorResponse handleSessionExpiredException(SessionException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        String errorLocation = extractErrorLocation(e.getStackTrace());

        log.error("""
                ===== SessionException 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                =================================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        String errorLocation = extractErrorLocation(e.getStackTrace());

        log.error("""
                ===== MethodArgumentNotValidException 발생 =====
                Validation Errors: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                =============================================
                """, 
                errors,
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(GenerationHashCodeException.class)
    public ErrorResponse handleGenerationHashCodeException(GenerationHashCodeException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        String errorLocation = extractErrorLocation(e.getStackTrace());

        log.error("""
                ===== GenerationHashCodeException 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                ==========================================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    @ExceptionHandler(ShareCodeException.class)
    public ErrorResponse handleShareCodeException(ShareCodeException e) {
        ErrorStatus errorStatus = e.getErrorStatus();

        String errorLocation = extractErrorLocation(e.getStackTrace());

        log.error("""
                ===== ShareCodeException 발생 =====
                ERROR CODE: {}
                ERROR MESSAGE: {}
                ERROR DESCRIPTION: {}
                발생 위치: {}
                Exception Message: {}
                Stack Trace: {}
                ===================================
                """, 
                errorStatus.getErrorCode(),
                errorStatus.getErrorMessage(),
                errorStatus.getErrorDescription(),
                errorLocation,
                e.getMessage(),
                e.getStackTrace()
        );
        return ErrorResponse.of(errorStatus);
    }

    private static String extractErrorLocation(StackTraceElement[] e) {
        return e.length > 0 ?
                e[0].getClassName() + "." + e[0].getMethodName() + "()" : "Unknown";
    }
}

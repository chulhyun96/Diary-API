package com.cheolhyeon.diary.app.exception;

import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.dto.ErrorResponse;
import com.cheolhyeon.diary.app.exception.friendrequest.FriendRequestErrorStatus;
import com.cheolhyeon.diary.app.exception.hashcode.GenerationHashCodeException;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
import com.cheolhyeon.diary.app.exception.session.SessionException;
import com.cheolhyeon.diary.app.exception.session.UserException;
import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        log.info("""
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String errorLocation = extractErrorLocation(e.getStackTrace());
        // 1) 루트 메시지 확보 (SQLException 까지 파고들어도 OK)
        String errorMessage = (e.getMessage() != null) ? e.getMessage() : "";
        if (!errorMessage.contains("Duplicate entry") || !errorMessage.contains("ux_friend_open_once")) {
            return null; // 우리가 처리할 케이스 아님
        }

        // 2) 'Duplicate entry '...'' 캡처
        Matcher m = Pattern.compile("Duplicate entry '([^']+)'").matcher(errorMessage);
        if (!m.find()) return null; // 패턴 불일치 시 다른 핸들러에 맡김

        // 메시지 예: '4384897461-4384897461-1'
        // 세 번째 토큰(= open_flag)은 버리고 2개만 쓰면 됨
        String[] parts = m.group(1).split("-", 3);
        if (parts.length < 2) return null;

        String ownerUserId = parts[0];
        String requesterUserId = parts[1];

        if (ownerUserId.equals(requesterUserId)) {
            log.info("자기 자신에게 친구 요청 시도: ownerUserId={}, requesterUserId={}, errorLocation={}",
                    ownerUserId, requesterUserId, errorLocation);
            return ErrorResponse.of(FriendRequestErrorStatus.CANNOT_REQUEST_TO_SELF);
        }

        log.info("중복 친구 요청 시도: ownerUserId={}, requesterUserId={}, errorLocation={}",
                ownerUserId, requesterUserId, errorLocation);
        return ErrorResponse.of(FriendRequestErrorStatus.ALREADY_REQUESTED);
    }

    private static String extractErrorLocation(StackTraceElement[] e) {
        return e.length > 0 ?
                e[0].getClassName() + "." + e[0].getMethodName() + "()" : "Unknown";
    }
}

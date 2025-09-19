package com.cheolhyeon.diary.app.exception.s3;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum S3ErrorStatus implements ErrorStatus {
    FAILED_UPLOAD_IMAGE(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "이미지 저장 실패"
    );
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

package com.cheolhyeon.diary.app.exception.s3;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public enum S3ErrorStatus implements ErrorStatus , Serializable {
    FAILED_UPLOAD_IMAGE(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "내부 서버 오류로 인해 이미지 저장이 실패했습니다."
    ),
    FAILED_LOAD_IMAGE(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "내부 서버 오류로 인해 이미지 불러오기가 실패했습니다." ),
    FAILED_DELETE_IMAGE(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "내부 서버 오류로 이미지 삭제가 실패했습니다." )
    ;
    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

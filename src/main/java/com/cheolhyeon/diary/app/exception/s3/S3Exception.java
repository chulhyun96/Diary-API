package com.cheolhyeon.diary.app.exception.s3;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
public class S3Exception extends RuntimeException {
    private final ErrorStatus errorStatus;
    public S3Exception(ErrorStatus errorStatus, List<String> failedKey) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
        log.error("S3 Failed: {}", failedKey);
    }
}

package com.cheolhyeon.diary.app.exception.s3;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3Exception extends RuntimeException {
    public S3Exception(ErrorStatus errorStatus, String failedKey) {
        super(errorStatus.getErrorDescription());
        log.error("S3 Failed: {}", failedKey);
    }
}

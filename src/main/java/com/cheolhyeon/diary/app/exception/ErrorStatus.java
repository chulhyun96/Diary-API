package com.cheolhyeon.diary.app.exception;

public interface ErrorStatus {
    int getErrorCode();

    String getErrorMessage();

    String getErrorDescription();
}

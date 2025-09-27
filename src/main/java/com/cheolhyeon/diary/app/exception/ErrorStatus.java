package com.cheolhyeon.diary.app.exception;

import java.io.Serializable;

public interface ErrorStatus extends Serializable {
    int getErrorCode();


    String getErrorMessage();

    String getErrorDescription();
}

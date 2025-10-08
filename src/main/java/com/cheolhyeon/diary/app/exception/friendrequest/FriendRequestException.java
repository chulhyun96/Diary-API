package com.cheolhyeon.diary.app.exception.friendrequest;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.Getter;

@Getter
public class FriendRequestException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public FriendRequestException(ErrorStatus errorStatus) {
        super(errorStatus.getErrorDescription());
        this.errorStatus = errorStatus;
    }
}

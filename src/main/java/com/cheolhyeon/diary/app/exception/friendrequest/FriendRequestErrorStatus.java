package com.cheolhyeon.diary.app.exception.friendrequest;

import com.cheolhyeon.diary.app.exception.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum FriendRequestErrorStatus implements ErrorStatus {
    CANNOT_REQUEST_TO_SELF(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "자기 자신에게 친구요청을 보낼 수 없습니다."),
    ALREADY_REQUESTED(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            "이미 대상자에게 친구요청을 보냈습니다."),
    ALREADY_DECIDED_REQUEST(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            "이미 처리된 항목입니다.")
    ;


    private final int errorCode;
    private final String errorMessage;
    private final String errorDescription;
}

package com.cheolhyeon.diary.friendrequest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestActionRequest {
    private String id;
    private String decide;
}

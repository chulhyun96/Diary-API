package com.cheolhyeon.diary.friendrequest.entity;

import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {
    @Id
    private String requestId;
    private Long ownerUserId;
    private Long requesterUserId;
    @Column(name = "code_id")
    private String hashShareCode;

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}

package com.cheolhyeon.diary.friendrequest.controller;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.dto.request.FriendRequestActionRequest;
import com.cheolhyeon.diary.friendrequest.dto.response.FriendRequestActionResponse;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    // shareCode로 공유 코드 주인 검색
    @GetMapping("/api/friend-request/{plainShareCode}")
    public ResponseEntity<?> searchOwnerByShareCode(@PathVariable("plainShareCode") String plainShareCode) {
        return ResponseEntity.ok(friendRequestService.searchOwnerByShareCode(plainShareCode));
    }
    // 친구 요청
    @PostMapping("/api/friend-request/{hashShareCode}")
    public ResponseEntity<?> reqeustFriendRequest(
            @PathVariable("hashShareCode") String hashShareCode,
            @CurrentUser CustomUserPrincipal user) {
        friendRequestService.requestFriendRequest(hashShareCode, user.getUserId());
        return ResponseEntity.ok().build();
    }
    //친구 요청 수락 및 거절 - Action
    @PostMapping("/api/friend-request")
    public ResponseEntity<?> receiveFriendRequest(
            @CurrentUser CustomUserPrincipal user,
            @RequestBody FriendRequestActionRequest actionRequest) {
        FriendRequestStatus result = friendRequestService.decide(user.getUserId(), actionRequest);
        if (result == FriendRequestStatus.DECLINED) {
            return ResponseEntity.ok(new FriendRequestActionResponse("친구 요청을 거절하셨습니다."));
        }
        return ResponseEntity.ok(new FriendRequestActionResponse("친구 요청을 수락하셨습니다."));
    }
}

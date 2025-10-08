package com.cheolhyeon.diary.friendrequest.controller;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    @GetMapping("/api/friend-request/{plainShareCode}")
    public ResponseEntity<?> searchOwnerByShareCode(@PathVariable("plainShareCode") String plainShareCode) {
        return ResponseEntity.ok(friendRequestService.searchOwnerByShareCode(plainShareCode));
    }
    @PostMapping("/api/friend-request/{hashShareCode}")
    public ResponseEntity<?> reqeustFriendRequest(
            @PathVariable("hashShareCode") String hashShareCode,
            @CurrentUser CustomUserPrincipal user) {
        friendRequestService.requestFriendRequest(hashShareCode, user.getUserId());
        return null;
    }
}

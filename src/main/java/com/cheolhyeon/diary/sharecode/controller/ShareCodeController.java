package com.cheolhyeon.diary.sharecode.controller;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.sharecode.dto.request.ShareCodeCreateRequest;
import com.cheolhyeon.diary.sharecode.service.ShareCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class ShareCodeController {
    private final ShareCodeService shareCodeService;

    @PostMapping("/api/sharecode")
    public ResponseEntity<?> createShareCode(
            @CurrentUser CustomUserPrincipal currentUser,
            @Validated @RequestBody ShareCodeCreateRequest request) {
        return ResponseEntity.ok().body(shareCodeService.create(currentUser.getUserId(), request));
    }

    @PatchMapping("/api/sharecode")
    public ResponseEntity<?> updateShareCode(
            @CurrentUser CustomUserPrincipal currentUser,
            @Validated @RequestBody ShareCodeCreateRequest request
    ) {
        shareCodeService.updateShareCode(currentUser.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/sharecode")
    public ResponseEntity<?> softDeleteShareCode(@CurrentUser CustomUserPrincipal currentUser) {
        shareCodeService.deleteMyShareCode(currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/sharecode")
    public ResponseEntity<?> getShareCode(@CurrentUser CustomUserPrincipal user) {
        return ResponseEntity.ok().body(shareCodeService.readMyShareCode(user.getUserId()));
    }

}

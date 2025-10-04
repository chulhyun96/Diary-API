package com.cheolhyeon.diary.diary.controller;

import com.cheolhyeon.diary.app.annotation.CurrentUser;
import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryCreateRequest;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryUpdateRequest;
import com.cheolhyeon.diary.diary.dto.response.*;
import com.cheolhyeon.diary.diary.service.DiaryService;
import com.github.f4b6a3.ulid.Ulid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;


    @GetMapping("/api/diary/{year}/{month}/{day}")
    public ResponseEntity<List<DiaryResponseByMonthAndDay>> getDiariesByMonthAndDay(
            @CurrentUser CustomUserPrincipal user, @PathVariable int year, @PathVariable int month, @PathVariable int day) {
        List<DiaryResponseByMonthAndDay> diaryResponseByMonthAndDays = diaryService.readDiariesByMonthAndDay(user.getUserId(), year, month, day);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResponseByMonthAndDays);
    }

    @GetMapping("/api/diary/{year}/{month}")
    public ResponseEntity<List<DiaryResponseByYearAndMonth>> getDiariesByYearAndMonth(
            @CurrentUser CustomUserPrincipal user,
            @PathVariable int year,
            @PathVariable int month
    ) {
        return ResponseEntity.ok(diaryService.readDiariesByYearAndMonth(user.getUserId(), year, month));
    }

    @GetMapping("/api/diary/{diaryId}")
    public ResponseEntity<DiaryResponseById> getDiaryById(@PathVariable String diaryId) {
        byte[] diaryPk = Ulid.from(diaryId).toBytes();
        return ResponseEntity.
                status(HttpStatus.OK).
                body(diaryService.readDiaryById(diaryPk));
    }

    @PostMapping(
            value = "/api/diary",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DiaryCreateResponse> createDiary(
            @CurrentUser CustomUserPrincipal user,
            @RequestPart("diary") DiaryCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(diaryService.createDiary(user.getUserId(), request, images));
    }

    @PatchMapping("/api/diary/{diaryId}")
    public ResponseEntity<DiaryUpdateResponse> updateDiary(
            @RequestBody DiaryUpdateRequest request,
            @PathVariable String diaryId) {
        byte[] diaryPk = Ulid.from(diaryId).toBytes();
        return ResponseEntity.ok(diaryService.updateDiary(request, diaryPk));
    }

    @PatchMapping(
            value = "/api/diary/{diaryId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> updateImages(
            @PathVariable String diaryId,
            @RequestPart(value = "updateImages", required = false)
            List<MultipartFile> updateImages,
            @RequestParam(value = "deleteImageKeys", required = false) List<String> deleteImageKeys
    ) {
        byte[] diaryPk = Ulid.from(diaryId).toBytes();
        diaryService.updateImages(diaryPk, deleteImageKeys, updateImages);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/diary/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable String diaryId) {
        byte[] diaryPk = Ulid.from(diaryId).toBytes();
        diaryService.deleteDiary(diaryPk);
        return ResponseEntity.noContent().build();
    }
}

package com.cheolhyeon.diary.diary.controller;

import com.cheolhyeon.diary.diary.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponse;
import com.cheolhyeon.diary.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/api/diary")
    public ResponseEntity<DiaryResponse> createDiary(
            @RequestPart("diary") DiaryRequest request,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(diaryService.createDiary(request, images));
    }
}

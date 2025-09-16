package com.cheolhyeon.diary.diaries.controller;

import com.cheolhyeon.diary.diaries.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diaries.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/api/create")
    public ResponseEntity<HttpStatus> createPost(@RequestBody DiaryRequest request) {
        diaryService.createDiary(request);
        return null;
    }
}

package com.cheolhyeon.diary.diary.controller;

import com.cheolhyeon.diary.diary.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponse;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseByMonthAndDayRead;
import com.cheolhyeon.diary.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    // TODO : 그리고 특정 달의 작성한 모든 일기 보기 기능도 필요 -> ex: 7월에 작성된 모든 일기 보여줘.
    @PostMapping("/api/diary")
    public ResponseEntity<DiaryResponse> createDiary(
            @RequestPart("diary") DiaryRequest request,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(diaryService.createDiary(request, images));
    }

    @GetMapping("/api/diary/{year}/{month}/{day}")
    public ResponseEntity<List<DiaryResponseByMonthAndDayRead>> getDiariesByMonthAndDay(
            @PathVariable int year, @PathVariable int month, @PathVariable int day) {
        List<DiaryResponseByMonthAndDayRead> diaryResponseByMonthAndDayReads = diaryService.readDiariesByMonthAndDay(year, month, day);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResponseByMonthAndDayReads);
    }
}

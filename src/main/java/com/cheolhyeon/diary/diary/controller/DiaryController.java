package com.cheolhyeon.diary.diary.controller;

import com.cheolhyeon.diary.diary.dto.reqeust.DiaryCreateRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryCreateResponse;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseById;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseByMonthAndDay;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseByYearAndMonth;
import com.cheolhyeon.diary.diary.service.DiaryService;
import com.github.f4b6a3.ulid.Ulid;
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


    @PostMapping("/api/diary")
    public ResponseEntity<DiaryCreateResponse> createDiary(
            @RequestPart("diary") DiaryCreateRequest request,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(diaryService.createDiary(request, images));
    }

    @GetMapping("/api/diary/{year}/{month}/{day}")
    public ResponseEntity<List<DiaryResponseByMonthAndDay>> getDiariesByMonthAndDay(
            @PathVariable int year, @PathVariable int month, @PathVariable int day) {
        List<DiaryResponseByMonthAndDay> diaryResponseByMonthAndDays = diaryService.readDiariesByMonthAndDay(year, month, day);
        return ResponseEntity.status(HttpStatus.OK).body(diaryResponseByMonthAndDays);
    }

    @GetMapping("/api/diary/{year}/{month}")
    public ResponseEntity<List<DiaryResponseByYearAndMonth>> getDiariesByYearAndMonth(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(diaryService.readDiariesByYearAndMonth(year, month));
    }
    @GetMapping("/api/diary/{diaryId}")
    public ResponseEntity<DiaryResponseById> getDiaryById(@PathVariable String diaryId) {
        byte[] bytes = Ulid.from(diaryId).toBytes();
        return ResponseEntity.
                status(HttpStatus.OK).
                body(diaryService.readDiaryById(bytes));
    }
}

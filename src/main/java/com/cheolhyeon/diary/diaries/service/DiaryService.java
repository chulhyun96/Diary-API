package com.cheolhyeon.diary.diaries.service;

import com.cheolhyeon.diary.app.exception.user.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diaries.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diaries.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public HttpStatus createDiary(@RequestBody DiaryRequest request) {
        Long kakaoId = request.getWriterId();
        User writer = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));
        return null;
    }
}

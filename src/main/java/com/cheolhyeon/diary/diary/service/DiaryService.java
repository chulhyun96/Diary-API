package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.diary.DiaryErrorStatus;
import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.user.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponse;
import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public DiaryResponse createDiary(DiaryRequest request, List<MultipartFile> images) {
        Long kakaoId = request.getWriterId();
        User writer = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));
        List<String> keys = new ArrayList<>();
        try {
            keys = s3Service.upload(writer.getKakaoId(), images);
        } catch (IOException e) {
            throw new DiaryException(DiaryErrorStatus.FAILED_SAVE); // 이미지 업로드 실패시 전체 롤백
        }
        Diaries entity = DiaryRequest.toEntity(kakaoId, writer.getDisplayName(), keys, request);
        return DiaryResponse.toResponse(diaryRepository.save(entity));
    }
}

package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.user.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.app.util.UlidGenerator;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diary.dto.S3RollbackCleanup;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponse;
import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final ApplicationEventPublisher eventPublisher;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public DiaryResponse createDiary(DiaryRequest request, List<MultipartFile> images) {
        // Authentication에서 kakaoId를 빼오는 방향으로 재설계 해야함.
        // request에서 getWriterId 필드 제거 해야함.
        Long kakaoId = request.getWriterId();
        User writer = userRepository.findById(kakaoId)
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));

        List<String> keys = s3Service.upload(writer.getKakaoId(), images);
        eventPublisher.publishEvent(new S3RollbackCleanup(keys));

        byte[] ulid = UlidGenerator.generatorUlid();
        Diaries entity = DiaryRequest.toEntity(ulid, kakaoId, writer.getDisplayName(), keys, request);
        return DiaryResponse.toResponse(diaryRepository.save(entity));
    }

    public List<DiaryResponse> readDiary(Long kakaoId, int year, int month) {
        // Authentication KakaoId
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        diaryRepository.findByMonth(kakaoId, start, end);
        return null;

    }
}

package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.diary.DiaryErrorStatus;
import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.user.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.app.util.UlidGenerator;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diary.dto.S3RollbackCleanup;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryCreateRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryCreateResponse;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseById;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseByMonthAndDayRead;
import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final ApplicationEventPublisher eventPublisher;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final Long writerId = 4384897461L;

    @Transactional
    public DiaryCreateResponse createDiary(DiaryCreateRequest request, List<MultipartFile> images) {
        // Authentication에서 kakaoId를 빼오는 방향으로 재설계 해야함.
        // request에서 getWriterId 필드 제거 해야함.
        User writer = userRepository.findById(writerId) // 얘도 아마 제거 될 것임
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));

        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> keys = s3Service.upload(writer.getKakaoId(), diaryId, images);
        eventPublisher.publishEvent(new S3RollbackCleanup(keys));

        Diaries entity = DiaryCreateRequest.toEntity(diaryId, writerId, writer.getDisplayName(), keys, request);
        Diaries savedEntity = diaryRepository.save(entity);
        return DiaryCreateResponse.toResponse(savedEntity);
    }

    public List<DiaryResponseByMonthAndDayRead> readDiariesByMonthAndDay(int year, int month, int day) {
        LocalDate currentDate = LocalDate.of(year, month, day);
        LocalDateTime startDay = currentDate.atStartOfDay();
        LocalDateTime endDay = startDay.plusDays(1);

        List<Diaries> diariesByMonthAndDay = diaryRepository.findByMonthAndDay(
                writerId, startDay, endDay);
        List<String> thumbnailImageKeys = s3Service.getThumbnailImageKey(writerId, year, month, day, diariesByMonthAndDay);
        List<String> thumbnailImage = s3Service.createImageUrl(thumbnailImageKeys);
        return DiaryResponseByMonthAndDayRead.toResponse(diariesByMonthAndDay, thumbnailImage);
    }

    public DiaryResponseById getDiaryById(byte[] diaryId) {
        Diaries diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorStatus.NOT_FOUND));
        List<String> imageKeysJson = diary.getImageKeysJson();
        List<String> imageUrl = s3Service.createImageUrl(imageKeysJson);
        return DiaryResponseById.toResponse(diary, imageUrl);
    }
}

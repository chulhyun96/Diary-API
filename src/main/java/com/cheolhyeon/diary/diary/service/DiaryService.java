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
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryUpdateRequest;
import com.cheolhyeon.diary.diary.dto.response.*;
import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

        LocalDateTime currentDateTime = LocalDateTime.now();
        int year = currentDateTime.getYear();
        int month = currentDateTime.getMonthValue();
        int day = currentDateTime.getDayOfMonth();
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> keys = s3Service.upload(writer.getKakaoId(), diaryId, images, year, month, day);

        eventPublisher.publishEvent(new S3RollbackCleanup(keys));

        Diaries entity = DiaryCreateRequest.toEntity(diaryId, writerId, writer.getDisplayName(), keys, request);
        Diaries savedEntity = diaryRepository.save(entity);
        return DiaryCreateResponse.toResponse(savedEntity);
    }

    @Transactional
    public DiaryUpdateResponse updateDiary(DiaryUpdateRequest request, byte[] diaryId) {
        Diaries targetEntity = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorStatus.NOT_FOUND));
        targetEntity.update(request);
        return DiaryUpdateResponse.toResponse(targetEntity);
    }

    @Transactional
    public void updateImages(byte[] diaryId, List<String> deleteImageKeysJson, List<MultipartFile> newImages) {
        Diaries targetEntity = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorStatus.NOT_FOUND));
        // 현재 이미지의 키값들을 끌고온다
        List<String> currentImageKeysJson =
                Optional.ofNullable(targetEntity.getImageKeysJson())
                        .orElseGet(List::of);
        // 삭제할 이미지들을 끌고온다
        Set<String> toDelete = new LinkedHashSet<>(Optional.ofNullable(deleteImageKeysJson).orElseGet(List::of));
        // 삭제할 이미지들은 바로 지워준다
        if (!toDelete.isEmpty()) {
            for (String keyToDelete : toDelete) {
                s3Service.delete(keyToDelete);
            }
        }
        // 삭제할 이미지들은 S3에서 지웠고, 실제로 남은것들을 끌고 와준다.
        List<String> remaining = currentImageKeysJson.stream()
                .filter(k -> !toDelete.contains(k))
                .toList();

        // 5) 신규 업로드
        List<String> uploadedKeys = Collections.emptyList();
        if (newImages != null && !newImages.isEmpty()) {
            LocalDateTime createdAt = targetEntity.getCreatedAt();
            uploadedKeys = s3Service.upload(
                    targetEntity.getWriterId(),
                    diaryId,
                    newImages,
                    createdAt.getYear(),
                    createdAt.getMonthValue(),
                    createdAt.getDayOfMonth()
            );
        }
        // 6) 최종 키 배열 = remaining + uploaded
        List<String> finalKeys = new ArrayList<>(remaining);
        finalKeys.addAll(uploadedKeys);

        targetEntity.updateImageKeysJson(finalKeys);
    }

    public List<DiaryResponseByYearAndMonth> readDiariesByYearAndMonth(int year, int month) {
        LocalDate searchDate = LocalDate.of(year, month, 1);
        LocalDateTime startMonth = searchDate.atStartOfDay();
        LocalDateTime endMonth = startMonth.plusMonths(1);

        List<Diaries> targetEntities = diaryRepository.findAllByYearAndMonth(writerId, startMonth, endMonth);
        List<String> thumbnailImageKeys =
                s3Service.getThumbnailImageKey(writerId, year, month, targetEntities);
        List<String> thumbnailImage = s3Service.createImageUrl(thumbnailImageKeys);
        return DiaryResponseByYearAndMonth.toResponse(targetEntities, thumbnailImage);
    }

    public List<DiaryResponseByMonthAndDay> readDiariesByMonthAndDay(int year, int month, int day) {
        LocalDate currentDate = LocalDate.of(year, month, day);
        LocalDateTime startDay = currentDate.atStartOfDay();
        LocalDateTime endDay = startDay.plusDays(1);

        List<Diaries> targetEntities = diaryRepository.findByMonthAndDay(
                writerId, startDay, endDay);
        List<String> thumbnailImageKeys = s3Service.getThumbnailImageKey(writerId, year, month, day, targetEntities);
        List<String> thumbnailImage = s3Service.createImageUrl(thumbnailImageKeys);
        return DiaryResponseByMonthAndDay.toResponse(targetEntities, thumbnailImage);
    }

    public DiaryResponseById readDiaryById(byte[] diaryId) {
        Diaries targetEntity = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorStatus.NOT_FOUND));
        if (targetEntity.getDeletedAt() != null) {
            throw new DiaryException(DiaryErrorStatus.NOT_FOUND);
        }
        List<String> imageKeysJson = targetEntity.getImageKeysJson();
        List<String> imageUrls = s3Service.createImageUrl(imageKeysJson);
        return DiaryResponseById.toResponse(targetEntity, imageKeysJson, imageUrls);
    }

    @Transactional
    public void deleteDiary(byte[] diaryPk) {
        Diaries targetEntity = diaryRepository.findById(diaryPk)
                .orElseThrow(() -> new DiaryException(DiaryErrorStatus.NOT_FOUND));
        if (targetEntity.getDeletedAt() != null) {
            throw new DiaryException(DiaryErrorStatus.ALREADY_DELETE);
        }
        targetEntity.softDeletedAt();
    }
}

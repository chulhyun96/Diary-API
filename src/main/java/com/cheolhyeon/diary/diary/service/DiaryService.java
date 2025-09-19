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
    private final Long kakaoId = 4384897461L;

    @Transactional
    public DiaryResponse createDiary(DiaryRequest request, List<MultipartFile> images) {
        // Authentication에서 kakaoId를 빼오는 방향으로 재설계 해야함.
        // request에서 getWriterId 필드 제거 해야함.
        User writer = userRepository.findById(kakaoId) // 얘도 아마 제거 될 것임
                .orElseThrow(() -> new UserException(UserErrorStatus.NOT_FOUND));

        List<String> keys = s3Service.upload(writer.getKakaoId(), images);
        eventPublisher.publishEvent(new S3RollbackCleanup(keys));

        byte[] ulid = UlidGenerator.generatorUlid();
        Diaries entity = DiaryRequest.toEntity(ulid, kakaoId, writer.getDisplayName(), keys, request);
        return DiaryResponse.toResponse(diaryRepository.save(entity));
    }

    public List<DiaryResponse> readDiaryByMonthAndDay(int year, int month, int day) {
        LocalDate currentDate = LocalDate.of(year, month, day);
        LocalDateTime startDay = currentDate.atStartOfDay();
        LocalDateTime endDay = startDay.plusDays(1);

        List<Diaries> diariesByMonth = diaryRepository.findByMonthAndDay(
                kakaoId, startDay, endDay);
        // 각각의 다이어리가 있고, 거기에 각각의 썸네일들이 있을 것임. 썸네일 택안했으면 그냥 없어도 되는거.
        // 왜냐면 여기서는 썸네일 이미지 한장이랑, 다이어리의 데이터만 넘기면 되는것임.
        // 썸네일 이미지 한장(없으면 없는데로) + 기본 DiaryResponse의 데이터들을 List로 반환하면 된다.
        s3Service.getThumbnail(diariesByMonth);
        return null;
    }
}

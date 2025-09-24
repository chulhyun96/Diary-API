package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.diary.DiaryErrorStatus;
import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.app.exception.s3.S3ErrorStatus;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
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
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
import com.cheolhyeon.diary.diary.repository.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {
    @Mock
    ApplicationEventPublisher applicationEventPublisher;
    @Mock
    DiaryRepository diaryRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    S3Service s3Service;
    @Mock
    MultipartFile mockImage1;
    @Mock
    MultipartFile mockImage2;
    
    @InjectMocks
    DiaryService diaryService;

    @Test
    @DisplayName("일기 생성 성공 테스트")
    void createDiary_Success() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(
                writerId,
                "",
                "",
                displayName,
                null,
                null,
                null
        );

        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tags(List.of())
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1, mockImage2);
        List<String> s3Keys = Arrays.asList("key1", "key2");

        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(mockUser.getKakaoId()), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt())).willReturn(s3Keys);
        
        ArgumentCaptor<Diaries> diaryCaptor = ArgumentCaptor.forClass(Diaries.class);
        given(diaryRepository.save(diaryCaptor.capture())).willAnswer(invocation -> {
            return invocation.getArgument(0); // 전달받은 Diaries 객체를 그대로 반환
        });

        // When
        DiaryCreateResponse result = diaryService.createDiary(request, images);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isNotNull();

        verify(userRepository).findById(writerId);
        verify(s3Service).upload(eq(mockUser.getKakaoId()), any(byte[].class), eq(images), anyInt(), anyInt(), anyInt());
        ArgumentCaptor<S3RollbackCleanup> eventCaptor = ArgumentCaptor.forClass(S3RollbackCleanup.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getImageKeys()).isEqualTo(s3Keys);
        verify(diaryRepository).save(any(Diaries.class));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 예외 발생")
    void createDiary_UserNotFound_ThrowsException() {
        // Given
        Long writerId = 4384897461L;
        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .build();

        List<MultipartFile> images = Collections.singletonList(mockImage1);

        given(userRepository.findById(writerId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> diaryService.createDiary(request, images))
                .isInstanceOf(UserException.class)
                .hasMessage(UserErrorStatus.NOT_FOUND.getErrorDescription());

        verify(userRepository).findById(writerId);
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
        verify(diaryRepository, never()).save(any(Diaries.class));
    }

    @Test
    @DisplayName("이미지가 없는 경우에도 일기 생성 성공")
    void createDiary_NoImages_Success() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .build();

        List<MultipartFile> images = List.of();
        List<String> s3Keys = List.of();

        Diaries savedDiary = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer(displayName)
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .imageKeysJson(s3Keys)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(writerId)).willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(mockUser.getKakaoId()), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt())).willReturn(s3Keys);
        given(diaryRepository.save(any(Diaries.class))).willReturn(savedDiary);

        // When
        DiaryCreateResponse result = diaryService.createDiary(request, images);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isNotNull();

        verify(s3Service).upload(eq(mockUser.getKakaoId()), any(byte[].class), eq(images), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("S3Service 업로드 실패 시 예외 전파")
    void createDiary_S3UploadFailure_ThrowsException() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1);

        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(mockUser.getKakaoId()), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt()))
                .willThrow(new RuntimeException("S3 업로드 실패"));

        // When
        assertThatThrownBy(() -> diaryService.createDiary(request, images))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 업로드 실패");

        // Then
        verify(userRepository).findById(writerId);
        verify(s3Service).upload(eq(mockUser.getKakaoId()), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
        verify(diaryRepository, never()).save(any(Diaries.class));
    }

    @Test
    @DisplayName("이벤트 발행 검증")
    void createDiary_VerifyEventPublishing() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        List<MultipartFile> images = Collections.singletonList(mockImage1);
        List<String> s3Keys = Arrays.asList("key1", "key2");

        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(mockUser.getKakaoId()), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt()))
                .willReturn(s3Keys);
        given(diaryRepository.save(any(Diaries.class))).willAnswer(invocation -> {
            return invocation.getArgument(0); // 전달받은 Diaries 객체를 그대로 반환
        });

        // When
        diaryService.createDiary(request, images);

        // Then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(S3RollbackCleanup.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isNotNull();
    }
    @Test
    @DisplayName("특정 날짜에 작성된 일기리스트 제공 ")
    void readDiaries_YearAndMonthAndDay() {
        //given
        int year = 2025;
        int month = 9;
        int day = 19;
        Long writerId = 4384897461L;
        LocalDate currentDate = LocalDate.of(year, month, day);
        LocalDateTime startDay = currentDate.atStartOfDay();
        LocalDateTime endDay = startDay.plusDays(1);
        Diaries mockDiary = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tagsJson(List.of())
                .imageKeysJson(List.of("key1"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Diaries> mockDiaries = Collections.singletonList(mockDiary);
        List<String> thumbnailKeys = List.of("key1");
        List<String> thumbnailUrls = List.of("url1");

        given(diaryRepository.findByMonthAndDay(writerId, startDay, endDay))
                .willReturn(mockDiaries);
        given(s3Service.getThumbnailImageKey(writerId, year, month, day, mockDiaries))
                .willReturn(thumbnailKeys);
        given(s3Service.createImageUrl(thumbnailKeys))
                .willReturn(thumbnailUrls);
        // When
        List<DiaryResponseByMonthAndDay> result = diaryService.readDiariesByMonthAndDay(year, month, day);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 제목");
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("url1");

        verify(diaryRepository,times(1)).findByMonthAndDay(writerId, startDay, endDay);
        verify(s3Service,times(1)).getThumbnailImageKey(writerId, year, month, day, mockDiaries);
        verify(s3Service,times(1)).createImageUrl(thumbnailKeys);
    }

    @Test
    @DisplayName("특정 날짜에 일기가 없는 경우 빈 리스트 반환")
    void readDiariesByMonthAndDay_NoDiaries_ReturnsEmptyList() {
        // Given
        int year = 2024;
        int month = 1;
        int day = 1;
        Long writerId = 4384897461L;
        LocalDateTime startDay = LocalDateTime.of(year, month, day, 0, 0, 0);
        LocalDateTime endDay = startDay.plusDays(1);

        given(diaryRepository.findByMonthAndDay(writerId, startDay, endDay))
                .willReturn(List.of());
        given(s3Service.getThumbnailImageKey(writerId, year, month, day, List.of()))
                .willReturn(List.of());
        given(s3Service.createImageUrl(List.of()))
                .willReturn(List.of());

        // When
        List<DiaryResponseByMonthAndDay> result = diaryService.readDiariesByMonthAndDay(year, month, day);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(diaryRepository, times(1)).findByMonthAndDay(writerId, startDay, endDay);
        verify(s3Service,times(1)).getThumbnailImageKey(writerId, year, month, day, List.of());
        verify(s3Service,times(1)).createImageUrl(List.of());
    }

    @Test
    @DisplayName("S3Service에서 썸네일 키 조회 실패 시 예외 전파")
    void readDiariesByMonthAndDay_S3ThumbnailKeyFailure_ThrowsException() {
        // Given
        int year = 2024;
        int month = 1;
        int day = 1;
        Long writerId = 4384897461L;
        LocalDateTime startDay = LocalDateTime.of(year, month, day, 0, 0, 0);
        LocalDateTime endDay = startDay.plusDays(1);

        Diaries mockDiary = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findByMonthAndDay(writerId, startDay, endDay))
                .willReturn(Collections.singletonList(mockDiary));
        given(s3Service.getThumbnailImageKey(writerId, year, month, day, Collections.singletonList(mockDiary)))
                .willThrow(new RuntimeException("S3 썸네일 키 조회 실패"));

        // When
        assertThatThrownBy(() -> diaryService.readDiariesByMonthAndDay(year, month, day))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 썸네일 키 조회 실패");
        // Then
        verify(diaryRepository).findByMonthAndDay(writerId, startDay, endDay);
        verify(s3Service).getThumbnailImageKey(writerId, year, month, day, Collections.singletonList(mockDiary));
        verify(s3Service, never()).createImageUrl(anyList());
    }

    @Test
    @DisplayName("일기 ID로 조회 시 일기를 찾을 수 없으면 DiaryException 발생")
    void getDiaryById_DiaryNotFound_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        
        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> diaryService.readDiaryById(diaryId))
                .isInstanceOf(DiaryException.class)
                .hasMessage(DiaryErrorStatus.NOT_FOUND.getErrorDescription());
        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service, never()).createImageUrl(anyList());
    }

    @Test
    @DisplayName("일기 ID로 조회 시 S3 이미지 URL 생성 실패하면 S3Exception 발생")
    void getDiaryById_S3ImageUrlCreationFailure_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        Diaries mockDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tagsJson(List.of("테스트", "일기"))
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(mockDiary));
        given(s3Service.createImageUrl(anyList()))
                .willThrow(new S3Exception(S3ErrorStatus.FAILED_UPLOAD_IMAGE, List.of("S3 이미지 URL 생성 실패")));

        // When
        assertThatThrownBy(() -> diaryService.readDiaryById(diaryId))
                .isInstanceOf(S3Exception.class)
                .hasMessage(S3ErrorStatus.FAILED_UPLOAD_IMAGE.getErrorDescription());

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service).createImageUrl(mockDiary.getImageKeysJson());
    }

    @Test
    @DisplayName("일기 ID로 조회 성공 시 DiaryResponseById 반환")
    void getDiaryById_Success_ReturnsDiaryResponseById() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> imageKeys = List.of("key1", "key2");
        List<String> imageUrls = List.of("url1", "url2");
        
        Diaries mockDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tagsJson(List.of("테스트", "일기"))
                .imageKeysJson(imageKeys)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(mockDiary));
        given(s3Service.createImageUrl(imageKeys))
                .willReturn(imageUrls);

        // When
        DiaryResponseById result = diaryService.readDiaryById(diaryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isEqualTo(diaryId);
        assertThat(result.getWriter()).isEqualTo("테스트유저");
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        assertThat(result.getMood()).isEqualTo(Mood.HAPPY);
        assertThat(result.getWeather()).isEqualTo(Weather.SUNNY);
        assertThat(result.getTags()).isEqualTo(List.of("테스트", "일기"));
        assertThat(result.getImageUrls()).isEqualTo(imageUrls);

        verify(diaryRepository).findById(diaryId);
        verify(s3Service).createImageUrl(imageKeys);
    }

    @Test
    @DisplayName("년/월로 일기 조회 시 정확한 리스트 반환")
    void readDiariesByYearAndMonth_Success_ReturnsCorrectList() {
        // Given
        int year = 2025;
        int month = 9;
        Long writerId = 4384897461L;
        LocalDate searchDate = LocalDate.of(year, month, 1);
        LocalDateTime startMonth = searchDate.atStartOfDay();
        LocalDateTime endMonth = startMonth.plusMonths(1);

        Diaries diary1 = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer("테스트유저1")
                .title("9월 첫 번째 일기")
                .content("9월 첫 번째 일기 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tagsJson(List.of("9월", "첫째"))
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.of(2025, 9, 5, 10, 30))
                .updatedAt(LocalDateTime.of(2025, 9, 5, 10, 30))
                .build();

        Diaries diary2 = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer("테스트유저2")
                .title("9월 두 번째 일기")
                .content("9월 두 번째 일기 내용")
                .mood(Mood.SAD)
                .weather(Weather.RAIN)
                .location(new Location())
                .tagsJson(List.of("9월", "둘째"))
                .imageKeysJson(List.of("key3", "key4"))
                .createdAt(LocalDateTime.of(2025, 9, 15, 14, 20))
                .updatedAt(LocalDateTime.of(2025, 9, 15, 14, 20))
                .build();

        List<Diaries> mockDiaries = List.of(diary1, diary2);
        List<String> thumbnailKeys = List.of("thumbnail_key1", "thumbnail_key2");
        List<String> thumbnailUrls = List.of("https://s3.url1", "https://s3.url2");

        given(diaryRepository.findAllByYearAndMonth(writerId, startMonth, endMonth))
                .willReturn(mockDiaries);
        given(s3Service.getThumbnailImageKey(writerId, year, month, mockDiaries))
                .willReturn(thumbnailKeys);
        given(s3Service.createImageUrl(thumbnailKeys))
                .willReturn(thumbnailUrls);

        // When
        List<DiaryResponseByYearAndMonth> result = diaryService.readDiariesByYearAndMonth(year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // 첫 번째 일기 검증
        DiaryResponseByYearAndMonth firstDiary = result.get(0);
        assertThat(firstDiary.getDiaryIdString()).isNotNull();
        assertThat(firstDiary.getDisplayName()).isEqualTo("테스트유저1");
        assertThat(firstDiary.getTitle()).isEqualTo("9월 첫 번째 일기");
        assertThat(firstDiary.getContent()).isEqualTo("9월 첫 번째 일기 내용");
        assertThat(firstDiary.getMood()).isEqualTo(Mood.HAPPY);
        assertThat(firstDiary.getWeather()).isEqualTo(Weather.SUNNY);
        assertThat(firstDiary.getThumbnailUrl()).isEqualTo("https://s3.url1");
        assertThat(firstDiary.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 9, 5, 10, 30));

        // 두 번째 일기 검증
        DiaryResponseByYearAndMonth secondDiary = result.get(1);
        assertThat(secondDiary.getDiaryIdString()).isNotNull();
        assertThat(secondDiary.getDisplayName()).isEqualTo("테스트유저2");
        assertThat(secondDiary.getTitle()).isEqualTo("9월 두 번째 일기");
        assertThat(secondDiary.getContent()).isEqualTo("9월 두 번째 일기 내용");
        assertThat(secondDiary.getMood()).isEqualTo(Mood.SAD);
        assertThat(secondDiary.getWeather()).isEqualTo(Weather.RAIN);
        assertThat(secondDiary.getThumbnailUrl()).isEqualTo("https://s3.url2");
        assertThat(secondDiary.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 9, 15, 14, 20));

        verify(diaryRepository).findAllByYearAndMonth(writerId, startMonth, endMonth);
        verify(s3Service).getThumbnailImageKey(writerId, year, month, mockDiaries);
        verify(s3Service).createImageUrl(thumbnailKeys);
    }

    @Test
    @DisplayName("년/월로 일기 조회 시 해당 월에 일기가 없으면 빈 리스트 반환")
    void readDiariesByYearAndMonth_NoDiaries_ReturnsEmptyList() {
        // Given
        int year = 2025;
        int month = 10;
        Long writerId = 4384897461L;
        LocalDate searchDate = LocalDate.of(year, month, 1);
        LocalDateTime startMonth = searchDate.atStartOfDay();
        LocalDateTime endMonth = startMonth.plusMonths(1);

        List<Diaries> emptyDiaries = List.of();
        List<String> emptyThumbnailKeys = List.of();
        List<String> emptyThumbnailUrls = List.of();

        // Mock 설정
        given(diaryRepository.findAllByYearAndMonth(writerId, startMonth, endMonth))
                .willReturn(emptyDiaries);
        given(s3Service.getThumbnailImageKey(writerId, year, month, emptyDiaries))
                .willReturn(emptyThumbnailKeys);
        given(s3Service.createImageUrl(emptyThumbnailKeys))
                .willReturn(emptyThumbnailUrls);

        // When
        List<DiaryResponseByYearAndMonth> result = diaryService.readDiariesByYearAndMonth(year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Mock 호출 검증
        verify(diaryRepository).findAllByYearAndMonth(writerId, startMonth, endMonth);
        verify(s3Service).getThumbnailImageKey(writerId, year, month, emptyDiaries);
        verify(s3Service).createImageUrl(emptyThumbnailKeys);
    }

    @Test
    @DisplayName("년/월로 일기 조회 시 S3 썸네일 키 조회 실패하면 예외 발생")
    void readDiariesByYearAndMonth_S3ThumbnailKeyFailure_ThrowsException() {
        // Given
        int year = 2025;
        int month = 9;
        Long writerId = 4384897461L;
        LocalDate searchDate = LocalDate.of(year, month, 1);
        LocalDateTime startMonth = searchDate.atStartOfDay();
        LocalDateTime endMonth = startMonth.plusMonths(1);

        Diaries mockDiary = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer("테스트유저")
                .title("테스트 일기")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location(new Location())
                .tagsJson(List.of())
                .imageKeysJson(List.of("key1"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Diaries> mockDiaries = List.of(mockDiary);

        // Mock 설정
        given(diaryRepository.findAllByYearAndMonth(writerId, startMonth, endMonth))
                .willReturn(mockDiaries);
        given(s3Service.getThumbnailImageKey(writerId, year, month, mockDiaries))
                .willThrow(new S3Exception(S3ErrorStatus.FAILED_LOAD_IMAGE, List.of("S3 썸네일 키 조회 실패")));

        // When
        assertThatThrownBy(() -> diaryService.readDiariesByYearAndMonth(year, month))
                .isInstanceOf(S3Exception.class)
                .hasMessage(S3ErrorStatus.FAILED_LOAD_IMAGE.getErrorDescription());
        // Then
        verify(diaryRepository).findAllByYearAndMonth(writerId, startMonth, endMonth);
        verify(s3Service).getThumbnailImageKey(writerId, year, month, mockDiaries);
        verify(s3Service, never()).createImageUrl(anyList());
    }

    @Test
    @DisplayName("다이어리 수정 성공 테스트")
    void updateDiary_Success() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .mood(Mood.SAD)
                .tags(List.of("수정", "테스트"))
                .build();

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("기존 제목")
                .content("기존 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .tagsJson(List.of("기존", "태그"))
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));

        // When
        DiaryUpdateResponse result = diaryService.updateDiary(request, diaryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.getImageCount()).isEqualTo(2);

        verify(diaryRepository).findById(diaryId);
    }

    @Test
    @DisplayName("다이어리 수정 시 일기를 찾을 수 없으면 DiaryException 발생")
    void updateDiary_DiaryNotFound_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> diaryService.updateDiary(request, diaryId))
                .isInstanceOf(DiaryException.class)
                .hasMessage(DiaryErrorStatus.NOT_FOUND.getErrorDescription());

        verify(diaryRepository).findById(diaryId);
    }

    @Test
    @DisplayName("이미지 업데이트 - 삭제만 수행")
    void updateImages_DeleteOnly_Success() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of("key1", "key2");
        List<MultipartFile> newImages = List.of();

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("key1", "key2", "key3"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));

        // When
        diaryService.updateImages(diaryId, deleteImageKeys, newImages);

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service, times(2)).delete(anyString()); // key1, key2 삭제
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 - 추가만 수행")
    void updateImages_AddOnly_Success() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of();
        List<MultipartFile> newImages = Arrays.asList(mockImage1, mockImage2);
        List<String> newImageKeys = List.of("newKey1", "newKey2");

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("existingKey1", "existingKey2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));
        given(s3Service.upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt()))
                .willReturn(newImageKeys);

        // When
        diaryService.updateImages(diaryId, deleteImageKeys, newImages);

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service, never()).delete(anyString());
        verify(s3Service).upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 - 삭제와 추가 동시 수행")
    void updateImages_DeleteAndAdd_Success() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of("key1");
        List<MultipartFile> newImages = Arrays.asList(mockImage1);
        List<String> newImageKeys = List.of("newKey1");

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("key1", "key2", "key3"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));
        given(s3Service.upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt()))
                .willReturn(newImageKeys);

        // When
        diaryService.updateImages(diaryId, deleteImageKeys, newImages);

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service).delete("key1");
        verify(s3Service).upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 시 일기를 찾을 수 없으면 DiaryException 발생")
    void updateImages_DiaryNotFound_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of("key1");
        List<MultipartFile> newImages = List.of();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> diaryService.updateImages(diaryId, deleteImageKeys, newImages))
                .isInstanceOf(DiaryException.class)
                .hasMessage(DiaryErrorStatus.NOT_FOUND.getErrorDescription());

        verify(diaryRepository).findById(diaryId);
        verify(s3Service, never()).delete(anyString());
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 - 아무것도 변경하지 않은 경우")
    void updateImages_NoChanges_Success() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of();
        List<MultipartFile> newImages = List.of();

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));

        // When
        diaryService.updateImages(diaryId, deleteImageKeys, newImages);

        // Then
        verify(diaryRepository).findById(diaryId);
        verify(s3Service, never()).delete(anyString());
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 - S3 삭제 실패 시 예외 전파")
    void updateImages_S3DeleteFailure_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of("key1");
        List<MultipartFile> newImages = List.of();

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));
        doThrow(new RuntimeException("S3 삭제 실패"))
                .when(s3Service).delete("key1");

        // When & Then
        assertThatThrownBy(() -> diaryService.updateImages(diaryId, deleteImageKeys, newImages))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 삭제 실패");

        verify(diaryRepository).findById(diaryId);
        verify(s3Service).delete("key1");
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이미지 업데이트 - S3 업로드 실패 시 예외 전파")
    void updateImages_S3UploadFailure_ThrowsException() {
        // Given
        byte[] diaryId = UlidGenerator.generatorUlid();
        List<String> deleteImageKeys = List.of();
        List<MultipartFile> newImages = Arrays.asList(mockImage1);

        Diaries existingDiary = Diaries.builder()
                .diaryId(diaryId)
                .writerId(1L)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(List.of("key1", "key2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findById(diaryId))
                .willReturn(Optional.of(existingDiary));
        given(s3Service.upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt()))
                .willThrow(new RuntimeException("S3 업로드 실패"));

        // When & Then
        assertThatThrownBy(() -> diaryService.updateImages(diaryId, deleteImageKeys, newImages))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 업로드 실패");

        verify(diaryRepository).findById(diaryId);
        verify(s3Service, never()).delete(anyString());
        verify(s3Service).upload(eq(1L), eq(diaryId), eq(newImages), anyInt(), anyInt(), anyInt());
    }
}

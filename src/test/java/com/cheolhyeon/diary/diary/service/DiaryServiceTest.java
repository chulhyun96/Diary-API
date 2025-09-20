package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.user.UserErrorStatus;
import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.app.util.UlidGenerator;
import com.cheolhyeon.diary.auth.entity.User;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diary.dto.S3RollbackCleanup;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponse;
import com.cheolhyeon.diary.diary.dto.response.DiaryResponseByMonthAndDayRead;
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

        DiaryRequest request = DiaryRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location("서울")
                .tags("테스트,일기")
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1, mockImage2);
        List<String> s3Keys = Arrays.asList("key1", "key2");

        Diaries savedDiary = Diaries.builder()
                .diaryId(UlidGenerator.generatorUlid())
                .writerId(writerId)
                .writer(displayName)
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location("서울")
                .tagsJson("테스트,일기")
                .imageKeysJson(s3Keys)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(writerId), any(byte[].class), anyList())).willReturn(s3Keys);
        given(diaryRepository.save(any(Diaries.class))).willReturn(savedDiary);

        // When
        DiaryResponse result = diaryService.createDiary(request, images);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        assertThat(result.getMood()).isEqualTo(Mood.HAPPY);
        assertThat(result.getWeather()).isEqualTo(Weather.SUNNY);
        assertThat(result.getImagesJson()).isEqualTo(s3Keys);

        // Verify interactions
        verify(userRepository).findById(writerId);
        verify(s3Service).upload(eq(writerId), any(byte[].class), eq(images));
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
        DiaryRequest request = DiaryRequest.builder()
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
        verify(s3Service, never()).upload(anyLong(), any(byte[].class), anyList());
        verify(diaryRepository, never()).save(any(Diaries.class));
    }

    @Test
    @DisplayName("이미지가 없는 경우에도 일기 생성 성공")
    void createDiary_NoImages_Success() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryRequest request = DiaryRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .build();

        List<MultipartFile> images = Arrays.asList();
        List<String> s3Keys = Arrays.asList();

        Diaries savedDiary = Diaries.builder()
                .diaryId(new byte[]{1, 2, 3, 4})
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
        given(s3Service.upload(eq(writerId), any(byte[].class), anyList())).willReturn(s3Keys);
        given(diaryRepository.save(any(Diaries.class))).willReturn(savedDiary);

        // When
        DiaryResponse result = diaryService.createDiary(request, images);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getImagesJson()).isEmpty();

        verify(s3Service).upload(eq(writerId), any(byte[].class), eq(images));
    }

    @Test
    @DisplayName("S3Service 업로드 실패 시 예외 전파")
    void createDiary_S3UploadFailure_ThrowsException() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryRequest request = DiaryRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1);

        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(writerId), any(byte[].class), anyList()))
                .willThrow(new RuntimeException("S3 업로드 실패"));

        // When & Then
        assertThatThrownBy(() -> diaryService.createDiary(request, images))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 업로드 실패");

        verify(userRepository).findById(writerId);
        verify(s3Service).upload(eq(writerId), any(byte[].class), anyList());
        verify(diaryRepository, never()).save(any(Diaries.class));
    }

    @Test
    @DisplayName("이벤트 발행 검증")
    void createDiary_VerifyEventPublishing() {
        // Given
        Long writerId = 4384897461L;
        String displayName = "테스트유저";
        User mockUser = new User(writerId, "", "", displayName, null, null, null);

        DiaryRequest request = DiaryRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1);
        List<String> s3Keys = Arrays.asList("key1", "key2");

        given(userRepository.findById(writerId))
                .willReturn(Optional.of(mockUser));
        given(s3Service.upload(eq(writerId), any(byte[].class), anyList()))
                .willReturn(s3Keys);
        given(diaryRepository.save(any(Diaries.class)))
                .willReturn(mock(Diaries.class));

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
                .location("서울")
                .tagsJson("테스트,일기")
                .imageKeysJson(Arrays.asList("key1"))
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
        List<DiaryResponseByMonthAndDayRead> result = diaryService.readDiariesByMonthAndDay(year, month, day);

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
        List<DiaryResponseByMonthAndDayRead> result = diaryService.readDiariesByMonthAndDay(year, month, day);

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

        // When & Then
        assertThatThrownBy(() -> diaryService.readDiariesByMonthAndDay(year, month, day))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 썸네일 키 조회 실패");

        verify(diaryRepository).findByMonthAndDay(writerId, startDay, endDay);
        verify(s3Service).getThumbnailImageKey(writerId, year, month, day, Collections.singletonList(mockDiary));
        verify(s3Service, never()).createImageUrl(anyList());
    }
}

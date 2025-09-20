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
}

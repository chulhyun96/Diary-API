package com.cheolhyeon.diary.diary.controller;

import com.cheolhyeon.diary.app.exception.diary.DiaryErrorStatus;
import com.cheolhyeon.diary.app.exception.diary.DiaryException;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryCreateRequest;
import com.cheolhyeon.diary.diary.dto.reqeust.DiaryUpdateRequest;
import com.cheolhyeon.diary.diary.dto.response.*;
import com.cheolhyeon.diary.diary.service.DiaryService;
import com.github.f4b6a3.ulid.Ulid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryControllerTest {

    @Mock
    DiaryService diaryService;

    @Mock
    MultipartFile mockImage1;

    @Mock
    MultipartFile mockImage2;

    @InjectMocks
    DiaryController diaryController;

    @Test
    @DisplayName("다이어리 생성 API 테스트")
    void createDiary_Success() {
        // Given
        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        List<MultipartFile> images = Arrays.asList(mockImage1, mockImage2);
        DiaryCreateResponse expectedResponse = DiaryCreateResponse.builder()
                .diaryId("01K5GMK22MR1DZGJ0MD191NRJ6")
                .year(2025)
                .month(9)
                .day(23)
                .build();

        given(diaryService.createDiary(request, images))
                .willReturn(expectedResponse);

        // When
        ResponseEntity<DiaryCreateResponse> result = diaryController.createDiary(request, images);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getDiaryId()).isEqualTo("01K5GMK22MR1DZGJ0MD191NRJ6");
        assertThat(result.getBody().getYear()).isEqualTo(2025);
        assertThat(result.getBody().getMonth()).isEqualTo(9);
        assertThat(result.getBody().getDay()).isEqualTo(23);

        verify(diaryService).createDiary(request, images);
    }

    @Test
    @DisplayName("특정 날짜 다이어리 조회 API 테스트")
    void getDiariesByMonthAndDay_Success() {
        // Given
        int year = 2025;
        int month = 9;
        int day = 23;

        DiaryResponseByMonthAndDay mockDiary = DiaryResponseByMonthAndDay.builder()
                .diaryIdString("01K5GMK22MR1DZGJ0MD191NRJ6")
                .displayName("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .thumbnailUrl("https://s3.url/thumbnail.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<DiaryResponseByMonthAndDay> expectedResponse = Arrays.asList(mockDiary);

        given(diaryService.readDiariesByMonthAndDay(year, month, day))
                .willReturn(expectedResponse);

        // When
        ResponseEntity<List<DiaryResponseByMonthAndDay>> result = 
                diaryController.getDiariesByMonthAndDay(year, month, day);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(diaryService).readDiariesByMonthAndDay(year, month, day);
    }

    @Test
    @DisplayName("년/월 다이어리 조회 API 테스트")
    void getDiariesByYearAndMonth_Success() {
        // Given
        int year = 2025;
        int month = 9;

        DiaryResponseByYearAndMonth mockDiary = DiaryResponseByYearAndMonth.builder()
                .diaryIdString("01K5GMK22MR1DZGJ0MD191NRJ6")
                .displayName("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .thumbnailUrl("https://s3.url/thumbnail.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<DiaryResponseByYearAndMonth> expectedResponse = Arrays.asList(mockDiary);

        given(diaryService.readDiariesByYearAndMonth(year, month))
                .willReturn(expectedResponse);

        // When
        ResponseEntity<List<DiaryResponseByYearAndMonth>> result = 
                diaryController.getDiariesByYearAndMonth(year, month);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(diaryService).readDiariesByYearAndMonth(year, month);
    }

    @Test
    @DisplayName("다이어리 ID로 조회 API 테스트")
    void getDiaryById_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();

        DiaryResponseById expectedResponse = DiaryResponseById.builder()
                .diaryId(diaryIdBytes)
                .writer("테스트유저")
                .title("테스트 제목")
                .content("테스트 내용")
                .imageKeysJson(Arrays.asList("key1", "key2"))
                .imageUrls(Arrays.asList("url1", "url2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryService.readDiaryById(diaryIdBytes))
                .willReturn(expectedResponse);

        // When
        ResponseEntity<DiaryResponseById> result = diaryController.getDiaryById(diaryId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getBody().getImageKeysJson()).hasSize(2);
        assertThat(result.getBody().getImageUrls()).hasSize(2);

        verify(diaryService).readDiaryById(diaryIdBytes);
    }

    @Test
    @DisplayName("다이어리 수정 API 테스트")
    void updateDiary_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();

        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        DiaryUpdateResponse expectedResponse = DiaryUpdateResponse.builder()
                .diaryId(diaryId)
                .title("수정된 제목")
                .content("수정된 내용")
                .imageCount(2)
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryService.updateDiary(request, diaryIdBytes))
                .willReturn(expectedResponse);

        // When
        ResponseEntity<DiaryUpdateResponse> result = diaryController.updateDiary(request, diaryId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getBody().getContent()).isEqualTo("수정된 내용");
        assertThat(result.getBody().getImageCount()).isEqualTo(2);

        verify(diaryService).updateDiary(request, diaryIdBytes);
    }

    @Test
    @DisplayName("이미지 업데이트 API - 삭제만 수행")
    void updateImages_DeleteOnly_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();
        List<String> deleteImageKeys = Arrays.asList("key1", "key2");
        List<MultipartFile> updateImages = Arrays.asList();

        // When
        ResponseEntity<Void> result = diaryController.updateImages(diaryId, updateImages, deleteImageKeys);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(diaryService).updateImages(diaryIdBytes, deleteImageKeys, updateImages);
    }

    @Test
    @DisplayName("이미지 업데이트 API - 추가만 수행")
    void updateImages_AddOnly_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();
        List<String> deleteImageKeys = Arrays.asList();
        List<MultipartFile> updateImages = Arrays.asList(mockImage1, mockImage2);

        // When
        ResponseEntity<Void> result = diaryController.updateImages(diaryId, updateImages, deleteImageKeys);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(diaryService).updateImages(diaryIdBytes, deleteImageKeys, updateImages);
    }

    @Test
    @DisplayName("이미지 업데이트 API - 삭제와 추가 동시 수행")
    void updateImages_DeleteAndAdd_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();
        List<String> deleteImageKeys = Arrays.asList("key1");
        List<MultipartFile> updateImages = Arrays.asList(mockImage1);

        // When
        ResponseEntity<Void> result = diaryController.updateImages(diaryId, updateImages, deleteImageKeys);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(diaryService).updateImages(diaryIdBytes, deleteImageKeys, updateImages);
    }

    @Test
    @DisplayName("이미지 업데이트 API - 아무것도 변경하지 않은 경우")
    void updateImages_NoChanges_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();
        List<String> deleteImageKeys = Arrays.asList();
        List<MultipartFile> updateImages = Arrays.asList();

        // When
        ResponseEntity<Void> result = diaryController.updateImages(diaryId, updateImages, deleteImageKeys);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(diaryService).updateImages(diaryIdBytes, deleteImageKeys, updateImages);
    }

    @Test
    @DisplayName("다이어리 삭제 API 테스트")
    void deleteDiary_Success() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();

        // When
        ResponseEntity<Void> result = diaryController.deleteDiary(diaryId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(diaryService).deleteDiary(diaryIdBytes);
    }

    @Test
    @DisplayName("다이어리 삭제 API - 다이어리 없음 예외 발생")
    void deleteDiary_DiaryNotFound_ThrowsException() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();

        doThrow(new DiaryException(DiaryErrorStatus.NOT_FOUND))
                .when(diaryService).deleteDiary(diaryIdBytes);

        // When & Then
        assertThatThrownBy(() -> diaryController.deleteDiary(diaryId))
                .isInstanceOf(DiaryException.class)
                .hasMessage(DiaryErrorStatus.NOT_FOUND.getErrorDescription());

        verify(diaryService).deleteDiary(diaryIdBytes);
    }

    @Test
    @DisplayName("다이어리 삭제 API - 이미 삭제된 다이어리 예외 발생")
    void deleteDiary_AlreadyDeleted_ThrowsException() {
        // Given
        String diaryId = "01K5GMK22MR1DZGJ0MD191NRJ6";
        byte[] diaryIdBytes = Ulid.from(diaryId).toBytes();

        doThrow(new DiaryException(DiaryErrorStatus.ALREADY_DELETE))
                .when(diaryService).deleteDiary(diaryIdBytes);

        // When & Then
        assertThatThrownBy(() -> diaryController.deleteDiary(diaryId))
                .isInstanceOf(DiaryException.class)
                .hasMessage(DiaryErrorStatus.ALREADY_DELETE.getErrorDescription());

        verify(diaryService).deleteDiary(diaryIdBytes);
    }
}

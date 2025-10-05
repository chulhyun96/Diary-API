package com.cheolhyeon.diary.sharecode.controller;

import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.sharecode.dto.ShareCodeCreateResponse;
import com.cheolhyeon.diary.sharecode.dto.request.ShareCodeCreateRequest;
import com.cheolhyeon.diary.sharecode.service.ShareCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShareCodeControllerTest {

    @Mock
    ShareCodeService shareCodeService;

    @InjectMocks
    ShareCodeController shareCodeController;

    private final Long userId = 1L;
    private final CustomUserPrincipal testUser = new CustomUserPrincipal(userId, "test_session_id");

    @Test
    @DisplayName("공유 코드 생성 성공")
    void createShareCode_Success() {
        // Given
        ShareCodeCreateResponse response = ShareCodeCreateResponse.builder()
                .statusCode(200)
                .codePlain("TEST_CODE_123")
                .message("공유 코드가 성공적으로 생성되었습니다.")
                .build();

        ShareCodeCreateRequest request = createShareCodeRequest("TEST_CODE_123");
        given(shareCodeService.create(anyLong(), any(ShareCodeCreateRequest.class))).willReturn(response);

        // When
        ResponseEntity<?> result = shareCodeController.createShareCode(testUser, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(ShareCodeCreateResponse.class);
        verify(shareCodeService).create(userId, request);
    }

    @Test
    @DisplayName("공유 코드 업데이트 성공")
    void updateShareCode_Success() {
        // Given
        ShareCodeCreateRequest request = createShareCodeRequest("UPDATED_CODE");
        
        // When
        ResponseEntity<?> result = shareCodeController.updateShareCode(testUser, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(shareCodeService).updateShareCode(userId, request);
    }

    @Test
    @DisplayName("공유 코드 삭제 성공")
    void deleteShareCode_Success() {
        // Given
        // When
        ResponseEntity<?> result = shareCodeController.softDeleteShareCode(testUser);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(shareCodeService).deleteMyShareCode(userId);
    }

    @Test
    @DisplayName("공유 코드 조회 성공 - 코드가 존재하는 경우")
    void getShareCode_Success_WhenCodeExists() {
        // Given
        String expectedCode = "EXISTING_CODE";
        given(shareCodeService.readMyShareCode(anyLong())).willReturn(expectedCode);

        // When
        ResponseEntity<?> result = shareCodeController.getShareCode(testUser);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedCode);
        verify(shareCodeService).readMyShareCode(userId);
    }

    @Test
    @DisplayName("공유 코드 조회 성공 - 코드가 존재하지 않는 경우")
    void getShareCode_Success_WhenCodeNotExists() {
        // Given
        given(shareCodeService.readMyShareCode(anyLong())).willReturn("");

        // When
        ResponseEntity<?> result = shareCodeController.getShareCode(testUser);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("");
        verify(shareCodeService).readMyShareCode(userId);
    }

    private ShareCodeCreateRequest createShareCodeRequest(String code) {
        return ShareCodeCreateRequest.builder()
                .code(code)
                .build();
    }
}

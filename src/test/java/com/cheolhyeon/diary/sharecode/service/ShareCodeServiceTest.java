package com.cheolhyeon.diary.sharecode.service;

import com.cheolhyeon.diary.app.exception.sharecode.ShareCodeException;
import com.cheolhyeon.diary.app.util.HashCodeGenerator;
import com.cheolhyeon.diary.sharecode.dto.ShareCodeCreateResponse;
import com.cheolhyeon.diary.sharecode.dto.request.ShareCodeCreateRequest;
import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import com.cheolhyeon.diary.sharecode.enums.ShareCodeStatus;
import com.cheolhyeon.diary.sharecode.repository.ShareCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShareCodeService 단위 테스트")
class ShareCodeServiceTest {

    @Mock
    private ShareCodeRepository shareCodeRepository;

    @Mock
    private HashCodeGenerator hashCodeGenerator;

    @InjectMocks
    private ShareCodeService shareCodeService;

    private ShareCodeCreateRequest createRequest;
    private ShareCode existingShareCode;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        createRequest = createShareCodeRequest("TEST_CODE_123");

        existingShareCode = ShareCode.builder()
                .userId(testUserId)
                .codePlain("EXISTING_CODE")
                .codeHash("EXISTING_HASH")
                .status(ShareCodeStatus.ACTIVE)
                .build();
    }

    private ShareCodeCreateRequest createShareCodeRequest(String code) {
        return ShareCodeCreateRequest.builder()
                .code(code)
                .build();
    }

    @Test
    @DisplayName("공유 코드 생성 성공 - 기존 코드가 없는 경우")
    void createShareCode_Success_WhenNoExistingCode() {
        // Given
        when(hashCodeGenerator.generateShareCodeHash("TEST_CODE_123")).thenReturn("HASHED_CODE_123");
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.empty());
        when(shareCodeRepository.save(any(ShareCode.class))).thenReturn(existingShareCode);

        // When
        ShareCodeCreateResponse response = shareCodeService.create(testUserId, createRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getCodePlain()).isEqualTo("TEST_CODE_123");
        assertThat(response.getMessage()).isEqualTo("공유 코드가 성공적으로 생성되었습니다.");

        verify(hashCodeGenerator).generateShareCodeHash("TEST_CODE_123");
        verify(shareCodeRepository).findShareCodeById(testUserId);
        verify(shareCodeRepository).save(any(ShareCode.class));
    }

    @Test
    @DisplayName("공유 코드 생성 실패 - 이미 코드가 존재하는 경우")
    void createShareCode_Fail_WhenCodeAlreadyExists() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.of(existingShareCode));

        // When & Then
        assertThatThrownBy(() -> shareCodeService.create(testUserId, createRequest))
                .isInstanceOf(ShareCodeException.class);

        verify(shareCodeRepository).findShareCodeById(testUserId);
        verify(shareCodeRepository, never()).save(any(ShareCode.class));
    }

    @Test
    @DisplayName("공유 코드 업데이트 성공")
    void updateShareCode_Success() {
        // Given
        String newCode = "UPDATED_CODE_456";
        ShareCodeCreateRequest updateRequest = createShareCodeRequest(newCode);

        when(hashCodeGenerator.generateShareCodeHash(newCode)).thenReturn("UPDATED_HASH_456");
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.of(existingShareCode));

        // When
        shareCodeService.updateShareCode(testUserId, updateRequest);

        // Then
        verify(hashCodeGenerator).generateShareCodeHash(newCode);
        verify(shareCodeRepository).findShareCodeById(testUserId);
        assertThat(existingShareCode.getCodePlain()).isEqualTo(newCode);
        assertThat(existingShareCode.getCodeHash()).isNotNull();
        assertThat(existingShareCode.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("공유 코드 업데이트 실패 - 코드가 존재하지 않는 경우")
    void updateShareCode_Fail_WhenCodeNotFound() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> shareCodeService.updateShareCode(testUserId, createRequest))
                .isInstanceOf(ShareCodeException.class);

        verify(shareCodeRepository).findShareCodeById(testUserId);
    }

    @Test
    @DisplayName("공유 코드 조회 성공 - 코드가 존재하는 경우")
    void readMyShareCode_Success_WhenCodeExists() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.of(existingShareCode));

        // When
        String result = shareCodeService.readMyShareCode(testUserId);

        // Then
        assertThat(result).isEqualTo("EXISTING_CODE");
        verify(shareCodeRepository).findShareCodeById(testUserId);
    }

    @Test
    @DisplayName("공유 코드 조회 성공 - 코드가 존재하지 않는 경우")
    void readMyShareCode_Success_WhenCodeNotExists() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.empty());

        // When
        String result = shareCodeService.readMyShareCode(testUserId);

        // Then
        assertThat(result).isEmpty();
        verify(shareCodeRepository).findShareCodeById(testUserId);
    }

    @Test
    @DisplayName("공유 코드 삭제 성공")
    void deleteMyShareCode_Success() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.of(existingShareCode));

        // When
        shareCodeService.deleteMyShareCode(testUserId);

        // Then
        verify(shareCodeRepository).findShareCodeById(testUserId);
        assertThat(existingShareCode.getStatus()).isEqualTo(ShareCodeStatus.REVOKED);
    }

    @Test
    @DisplayName("공유 코드 삭제 실패 - 코드가 존재하지 않는 경우")
    void deleteMyShareCode_Fail_WhenCodeNotFound() {
        // Given
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> shareCodeService.deleteMyShareCode(testUserId))
                .isInstanceOf(ShareCodeException.class);

        verify(shareCodeRepository).findShareCodeById(testUserId);
    }

    @Test
    @DisplayName("해시 생성 테스트 - create 메서드를 통한 간접 테스트")
    void generateShareCode_Hash_Test_ThroughCreate() {
        // Given
        when(hashCodeGenerator.generateShareCodeHash("TEST_CODE_123")).thenReturn("HASHED_CODE_123");
        when(shareCodeRepository.findShareCodeById(testUserId)).thenReturn(Optional.empty());
        when(shareCodeRepository.save(any(ShareCode.class))).thenReturn(existingShareCode);

        // When
        ShareCodeCreateResponse response = shareCodeService.create(testUserId, createRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        // 해시가 정상적으로 생성되었는지 확인 (저장된 엔티티의 해시 필드 확인)
        verify(hashCodeGenerator).generateShareCodeHash("TEST_CODE_123");
        verify(shareCodeRepository).save(argThat(shareCode -> 
            shareCode.getCodeHash() != null && !shareCode.getCodeHash().isEmpty()));
    }
}
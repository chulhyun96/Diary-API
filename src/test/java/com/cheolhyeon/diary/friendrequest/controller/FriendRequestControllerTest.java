package com.cheolhyeon.diary.friendrequest.controller;

import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.dto.response.SearchShareCodeOwnerResponse;
import com.cheolhyeon.diary.friendrequest.service.FriendRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendRequestController 단위 테스트")
class FriendRequestControllerTest {

    @Mock
    private FriendRequestService friendRequestService;

    @InjectMocks
    private FriendRequestController friendRequestController;

    private final Long userId = 1L;
    private final String sessionId = "test-session-id";
    private final CustomUserPrincipal testUser = new CustomUserPrincipal(userId, sessionId);

    @Test
    @DisplayName("ShareCode로 소유자 검색 성공")
    void searchOwnerByShareCode_Success() {
        // Given
        String plainShareCode = "PLAIN_SHARE_CODE_123";
        String hashShareCode = "HASH_CODE_ABC123";
        String ownerDisplayName = "친구유저";

        SearchShareCodeOwnerResponse expectedResponse = SearchShareCodeOwnerResponse.builder()
                .ownerDisplayName(ownerDisplayName)
                .shareCodeHash(hashShareCode)
                .build();

        given(friendRequestService.searchOwnerByShareCode(anyString())).willReturn(expectedResponse);

        // When
        ResponseEntity<?> result = friendRequestController.searchOwnerByShareCode(plainShareCode);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(SearchShareCodeOwnerResponse.class).isNotNull();

        SearchShareCodeOwnerResponse responseBody = (SearchShareCodeOwnerResponse) result.getBody();
        assertThat(responseBody.getOwnerDisplayName()).isEqualTo(ownerDisplayName);
        assertThat(responseBody.getShareCodeHash()).isEqualTo(hashShareCode);

        verify(friendRequestService).searchOwnerByShareCode(plainShareCode);
    }

    @Test
    @DisplayName("친구 요청 전송 성공")
    void requestFriendRequest_Success() {
        // Given
        String hashShareCode = "HASH_CODE_ABC123";

        // When
        ResponseEntity<?> result = friendRequestController.reqeustFriendRequest(hashShareCode, testUser);

        // Then
        assertThat(result).isNull(); // Controller에서 null 반환

        verify(friendRequestService).requestFriendRequest(hashShareCode, userId);
    }

    @Test
    @DisplayName("친구 요청 전송 - 다양한 사용자로 요청")
    void requestFriendRequest_WithDifferentUsers() {
        // Given
        String hashShareCode = "HASH_CODE_XYZ789";
        Long differentUserId = 999L;
        CustomUserPrincipal differentUser = new CustomUserPrincipal(differentUserId, "different-session");

        // When
        friendRequestController.reqeustFriendRequest(hashShareCode, differentUser);

        // Then
        verify(friendRequestService).requestFriendRequest(hashShareCode, differentUserId);
    }
}


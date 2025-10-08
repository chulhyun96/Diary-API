package com.cheolhyeon.diary.friendrequest.controller;

import com.cheolhyeon.diary.auth.service.CustomUserPrincipal;
import com.cheolhyeon.diary.friendrequest.dto.request.FriendRequestActionRequest;
import com.cheolhyeon.diary.friendrequest.dto.response.FriendRequestActionResponse;
import com.cheolhyeon.diary.friendrequest.dto.response.SearchShareCodeOwnerResponse;
import com.cheolhyeon.diary.friendrequest.enums.FriendRequestStatus;
import com.cheolhyeon.diary.friendrequest.service.FriendRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
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
        assertThat(Objects.requireNonNull(responseBody).getOwnerDisplayName()).isEqualTo(ownerDisplayName);
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
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNull(); // ResponseEntity.ok().build()는 body가 null

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

    @Test
    @DisplayName("친구 요청 수락 성공")
    void receiveFriendRequest_Success_Accept() {
        // Given
        String requestId = "REQ_123456";
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        given(friendRequestService.decide(anyLong(), any(FriendRequestActionRequest.class)))
                .willReturn(FriendRequestStatus.ACCEPTED);

        // When
        ResponseEntity<?> result = friendRequestController.receiveFriendRequest(testUser, actionRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(FriendRequestActionResponse.class).isNotNull();

        FriendRequestActionResponse responseBody = (FriendRequestActionResponse) result.getBody();
        assertThat(Objects.requireNonNull(responseBody).getActionResult()).isEqualTo("친구 요청을 수락하셨습니다.");

        verify(friendRequestService).decide(userId, actionRequest);
    }

    @Test
    @DisplayName("친구 요청 거절 성공")
    void receiveFriendRequest_Success_Decline() {
        // Given
        String requestId = "REQ_789012";
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.DECLINED.name())
                .build();

        given(friendRequestService.decide(anyLong(), any(FriendRequestActionRequest.class)))
                .willReturn(FriendRequestStatus.DECLINED);

        // When
        ResponseEntity<?> result = friendRequestController.receiveFriendRequest(testUser, actionRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(FriendRequestActionResponse.class).isNotNull();

        FriendRequestActionResponse responseBody = (FriendRequestActionResponse) result.getBody();
        assertThat(Objects.requireNonNull(responseBody).getActionResult()).isEqualTo("친구 요청을 거절하셨습니다.");

        verify(friendRequestService).decide(userId, actionRequest);
    }

    @Test
    @DisplayName("친구 요청 처리 - 다양한 사용자로 요청")
    void receiveFriendRequest_WithDifferentUsers() {
        // Given
        Long differentUserId = 999L;
        CustomUserPrincipal differentUser = new CustomUserPrincipal(differentUserId, "different-session");
        
        String requestId = "REQ_DIFF_123";
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        given(friendRequestService.decide(anyLong(), any(FriendRequestActionRequest.class)))
                .willReturn(FriendRequestStatus.ACCEPTED);

        // When
        friendRequestController.receiveFriendRequest(differentUser, actionRequest);

        // Then
        verify(friendRequestService).decide(differentUserId, actionRequest);
    }

    @Test
    @DisplayName("친구 요청 처리 - 수락과 거절 응답 메시지 검증")
    void receiveFriendRequest_ResponseMessageValidation() {
        // Given
        String requestId = "REQ_MSG_TEST";
        FriendRequestActionRequest actionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.ACCEPTED.name())
                .build();

        // When - 수락
        given(friendRequestService.decide(anyLong(), any(FriendRequestActionRequest.class)))
                .willReturn(FriendRequestStatus.ACCEPTED);
        ResponseEntity<?> acceptResult = friendRequestController.receiveFriendRequest(testUser, actionRequest);

        // Then - 수락
        FriendRequestActionResponse acceptResponse = (FriendRequestActionResponse) acceptResult.getBody();
        assertThat(Objects.requireNonNull(acceptResponse).getActionResult()).isEqualTo("친구 요청을 수락하셨습니다.");

        // When - 거절
        FriendRequestActionRequest declineActionRequest = FriendRequestActionRequest.builder()
                .id(requestId)
                .decide(FriendRequestStatus.DECLINED.name())
                .build();
        given(friendRequestService.decide(anyLong(), any(FriendRequestActionRequest.class)))
                .willReturn(FriendRequestStatus.DECLINED);
        ResponseEntity<?> declineResult = friendRequestController.receiveFriendRequest(testUser, declineActionRequest);

        // Then - 거절
        FriendRequestActionResponse declineResponse = (FriendRequestActionResponse) declineResult.getBody();
        assertThat(Objects.requireNonNull(declineResponse).getActionResult()).isEqualTo("친구 요청을 거절하셨습니다.");
    }
}

